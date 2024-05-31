package net.kotlinx.gradle

import aws.sdk.kotlin.services.lambda.model.ResourceConflictException
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.code.CodedeployUtil
import net.kotlinx.aws.code.EcsDeployData
import net.kotlinx.aws.code.createDeployment
import net.kotlinx.aws.ecr.findAndUpdateTag
import net.kotlinx.aws.ecs.touch
import net.kotlinx.aws.lambda.*
import net.kotlinx.aws.s3.putObject
import net.kotlinx.concurrent.delay
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.retry.RetryTemplate
import net.kotlinx.system.DeploymentType
import net.kotlinx.system.OsType
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.TimeStart
import java.io.File
import kotlin.time.Duration.Companion.seconds

/**
 * 그래들 빌드용 유틸. 그냥 그래들에 하드코딩하면 너무 지저분해져서 만들었음
 * 그래들의 buildSrc + 이것을 조합해서 발드함.
 * buildSrc 까지 같이쓰는 이유는 프로젝트별로 설정이 조금씩 다르고, 여기는 그래들 의존성을 추가할 수 없어서임 (maven에 찾을 수 없음)
 *  */
class GradleBuilder {

    @Kdsl
    constructor(block: GradleBuilder.() -> Unit = {}) {
        apply(block)
    }

    /** AWS 인스턴스 */
    val aws by koinLazy<AwsClient>()

    /** AWS 프로파일 정보 */
    val profileName: String? = koin<AwsConfig>().profileName

    /**
     * AWS 로컬 커맨드 실행의 경우 프로파일을 추가로 입력해줘야함
     *  */
    val profileCommand = if (AwsInstanceTypeUtil.IS_LOCAL && profileName != null) "--profile $profileName" else ""

    /**
     * 기본 설정과는 다른 리즌에도 배포할 수 있어서 별도 지정
     *  */
    lateinit var awsConfig: AwsConfig

    /** 배포 타입 */
    var deploymentType: DeploymentType = DeploymentType.load()

    /** 자주 사용되는 접미어 */
    val suff: String
        get() = deploymentType.name.lowercase()

    /**
     * 이 빌드의 커밋 id 정보
     * AWS 코드빌드 환경의 경우 환경변수 설정을 통해 입력받을 수 있음
     * */
    var commitId: String = System.getenv("COMMIT_ID") ?: "unknown"

    /**
     * 브랜치 명 (최종 벨리데이션 체크에 사용)
     * 코드디플로이 -> 경우 환경변수에 넣어줌
     * 로컬 ->  git branch
     * */
    val branchName: String? = System.getenv("BRANCH_NAME")

    /** 기본 리트라이 */
    var awsRetry: RetryTemplate = RetryTemplate {
        interval = 3.seconds
        predicate = RetryTemplate.match(ResourceConflictException::class.java)
    }

    /** 로깅용 문구 */
    override fun toString(): String = "OS(${OsType.OS_TYPE}) : deploymentType/branchName(${deploymentType}/${branchName}) / commitId($commitId)"

    //==================================================== 커맨드 ======================================================

    /**
     * os에 따른 커멘드 라인을 리턴해줌
     * ex)  commandLine(build.command(command))
     * 일반 js build 명령은 cmd 등이 앞에 있어야 하고 AWS 호출은 cmd 없어도 됨..
     * 한번에 한개의 커맨드만 exec{} 안에 둘것!
     * js 번들링의 경우 각 플젝 루트에서 실행하면 됨
     * ex) npx vite build
     * ex) aws s3 sync ${project(":demo-svelte").projectDir}\dist s3://demo.kotlinx.net/
     * */
    @Deprecated("OS 패키지꺼 쓰세요")
    fun command(command: String): List<String> = when (OsType.OS_TYPE) {
        OsType.LINUX -> listOf("bash", "-c", command)
        OsType.WINDOWS -> listOf("cmd", "/c", command)
        OsType.MAC -> listOf("bash", "-c", command) // mac 은 잘 모름
    }

    //==================================================== ECR 간단 배포 (실제배포는 jib) ======================================================

    /** ECR 태그이름 생성기 */
    var ecrTagName: () -> String = { "${deploymentType}-${TimeFormat.YMDHM_F02.get()}" }

    /** ECR 로그인 주소 생성 */
    fun ecrLoginCommand(repositoryName: String): String {
        val ecrUrl = awsConfig.ecrPath(repositoryName)
        return "aws ecr get-login-password --region ${awsConfig.region} $profileCommand | docker login --username AWS --password-stdin $ecrUrl"
    }

    //====================================================  ECS(ECR) 간단 배포 ======================================================

    /** ECS 배포 설정정보 */
    lateinit var ecsDeployData: EcsDeployData

    /** 롤링 배포 */
    fun ecrDeployRolling() {
        runBlocking {
            aws.ecs.touch(ecsDeployData.clusterName, ecsDeployData.serviceName)
            log.info { "[${ecsDeployData.serviceName}] 롤링 배포 완료" }
        }
    }

    /** 블루그린 배포 */
    fun ecrDeployBlueGreen() {
        runBlocking {
            val deployment = aws.codeDeploy.createDeployment(ecsDeployData)
            log.info { "[${ecsDeployData.containerName}] 코드디플로이 배포 완료 ->  ${CodedeployUtil.toConsoleLink(deployment.deploymentId!!)}" }
        }
    }

    //==================================================== 람다(ECR) 간단 배포 ======================================================

    /** RCR 람다 함수 업데이트 */
    fun lambdaUpdateFunction(repositoryName: String, functionName: String) {
        runBlocking {
            checkNotNull(branchName) { "branchName is required" }
            val tagName = ecrTagName()
            log.trace { "이번에 배포한 ECR을 찾아서 branchName 태그를 이동시켜줌" }
            aws.ecr.findAndUpdateTag(repositoryName, tagName, branchName)

            log.info { "람다 함수 ECR 터치" }
            val imageUrl = awsConfig.ecrPath(repositoryName)
            aws.lambda.updateFunctionCode(functionName, imageUrl, branchName) //터치만 해줌
        }
    }

    //==================================================== 람다(jar) 간단 배포 ======================================================

    /** 람다 업로드 버킷 생성기 */
    var lambdaS3Bucket: () -> String = { "${profileName}-work-${deploymentType.name.lowercase()}" }

    /** 람다 업로드 키 접미어 */
    var lambdaS3Prefix = "code"

    /** 람다 레이어 리스트 */
    lateinit var lambdaLayers: List<String>

    /** 레이어배포의 경우 단계별로 진행해야 해서 여기 단축함수를 넣음 */
    fun lambdaUpdateLayer(layerName: String, zipFile: File) {
        val aws by koinLazy<AwsClient>()
        val s3Bucket = lambdaS3Bucket()
        val s3Key = "${lambdaS3Prefix}/${layerName}/${zipFile.name}"
        runBlocking {
            val start = TimeStart()
            log.debug { "###### 레이어 파일(${zipFile.length() / 1024 / 1024}mb)을 업로드합니다... " }
            aws.s3.putObject(s3Bucket, s3Key, zipFile)
            log.info { "###### 레이어 파일(${zipFile.length() / 1024 / 1024}mb)을 업로드 완료 -> $start" }

            val layerArn = aws.lambda.publishLayerVersion(s3Bucket, s3Key, layerName)
            log.info { "###### 레이어를 업데이트 완료 -> $layerArn" }
        }
    }

    /** 람다배포의 경우 단계별로 진행해야 해서 여기 단축함수를 넣음 */
    fun lambdaUpdateFunction(functionName: String, jarFile: File, alias: String? = null) {
        val aws by koinLazy<AwsClient>()
        runBlocking {

            val layerArns = aws.lambda.listLayerVersions(lambdaLayers).map { it.layerVersionArn!! }
            aws.lambda.updateFunctionLayers(functionName, layerArns)
            log.info { "###### step1 람다[${functionName}] = 레이어 최신버전으로 업데이트" }
            layerArns.forEach { log.debug { " -> $it" } }

            log.trace { "코드반영까지 잠시 대기" }
            2.seconds.delay()
            awsRetry.withRetry {
                aws.lambda.updateFunctionCode(functionName, jarFile)
                log.info { "###### step2 람다 코드 업데이트 from ${jarFile.absolutePath} (${jarFile.length() / 1024 / 1024}mb)" }
            }

            alias?.let {
                3.seconds.delay() //코드반영까지 잠시 대기
                try {
                    val updatedVersion = aws.lambda.publishVersionAndUpdateAlias(functionName, it) //초기화시 오류나면 여기서 에러남
                    log.info { "###### step3 람다 버전 & alias 업데이트 -> (${updatedVersion})" }
                } catch (e: Throwable) {
                    log.error { "###### 스냅스타트에서 오류가 발생했습니다. 로그 확인해주세요" }
                    throw e
                }
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}