package net.kotlinx.gradle

import aws.sdk.kotlin.services.lambda.model.ResourceConflictException
import ch.qos.logback.classic.Level
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.code.CodedeployUtil
import net.kotlinx.aws.code.EcsDeployData
import net.kotlinx.aws.code.createDeployment
import net.kotlinx.aws.ecs.touch
import net.kotlinx.aws.lambda.*
import net.kotlinx.aws.s3.putObject
import net.kotlinx.concurrent.delay
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.logback.TempLogger
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
     * 기본 설정과는 다른 리즌에도 배포할 수 있어서 var 로 지정
     *  */
    var awsConfig: AwsConfig = koin<AwsConfig>()

    /** 배포 타입 */
    var deploymentType: DeploymentType = DeploymentType.load()

    /** 자주 사용되는 접미어 */
    val suff: String
        get() = deploymentType.name.lowercase()

    /**
     * 이 빌드의 커밋 id 정보
     * AWS 코드빌드 환경의 경우 환경변수 설정을 통해 입력받을 수 있음
     * */
    var commitId: String = System.getenv(AwsNaming.COMMIT_ID) ?: "unknown"

    /** 기본 리트라이 */
    var awsRetry: RetryTemplate = RetryTemplate {
        retries = 4
        interval = 20.seconds
        predicate = RetryTemplate.match(ResourceConflictException::class.java)
    }

    /** 람다 함수명 */
    lateinit var functionName: String

    /** 로깅용 문구 */
    override fun toString(): String = "OS(${OsType.OS_TYPE}) : deploymentType(${deploymentType}) / commitId($commitId)"

    //==================================================== ECR 간단 배포 (실제배포는 jib) ======================================================

    /**
     * ECR 태그이름 생성기.
     *  */
    var ecrTagNameGenerator: () -> String = { "${suff}_${TimeFormat.YMDHM_F02.get()}" }

    /** 태그 이뮤터블 */
    var immutable = false

    /**
     * ECR 태그이름
     * 불변 태그 사용시, 이걸로 이미지 정의
     * 가변 태그 사용시에는 단순 마킹용
     *  */
    val ecrTagName by lazy { ecrTagNameGenerator() }

    /**
     * 리파지토리 명
     * ex) job-dev
     *  */
    lateinit var ecrRepositoryName: String

    /** Repository 주소*/
    val ecrRepositoryUri: String by lazy { awsConfig.ecrPath(ecrRepositoryName) }

    /** ECR 로그인 명령어 */
    val ecrLoginCommand: String by lazy { "aws ecr get-login-password --region ${awsConfig.region} $profileCommand | docker login --username AWS --password-stdin $ecrRepositoryUri" }

    //==================================================== 람다(ECR) 간단 배포 ======================================================

    /** RCR 람다 함수 업데이트 */
    fun ecrUpdateLambda() {
        runBlocking {
            log.info { "[${functionName}] 람다 함수 $ecrTagName ECR 터치" }
            aws.lambda.updateFunctionCode(functionName, ecrRepositoryUri, ecrTagName) //터치만 해줌
        }
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

    //==================================================== 람다(jar) 간단 배포 ======================================================

    /** 람다 업로드 버킷 생성기 */
    var lambdaS3Bucket: () -> String = { "${profileName}-work-${deploymentType.name.lowercase()}" }

    /** 람다 업로드 키 접미어 */
    var lambdaS3Prefix = "code"

    /** 람다 레이어 리스트 */
    lateinit var lambdaLayers: List<String>

    /** 람다에 레이어가 반영되기를 기다리는 시간. 2초 내로 끝날때도 있고 오래 걸릴때도 있음 */
    var lambdaLayerApplyDelay = 5.seconds

    /** 담다에 코드가 반영되기를 기다리는 시간. */
    var lambdaCodeApplyDelay = 3.seconds

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

    /**
     * 람다배포의 경우 단계별로 진행해야 해서 여기 단축함수를 넣음
     * 펑션이 여러개일 수 있어서 functionName 를 별도 받는다.
     *  */
    fun lambdaUpdateFunction(functionName: String, jarFile: File, alias: String? = null) {
        val aws by koinLazy<AwsClient>()
        runBlocking {

            val layerArns = aws.lambda.listLayerVersions(lambdaLayers).map { it.layerVersionArn!! }
            log.info { "###### step1 람다[${functionName}] = 레이어 최신버전으로 업데이트" }
            aws.lambda.updateFunctionLayers(functionName, layerArns)
            layerArns.forEach { log.debug { " -> $it" } }

            log.info { " -> 업데이트된 레이어가 람다에 반영되는것을  ${lambdaLayerApplyDelay}동안 잠시 대기.." }
            lambdaLayerApplyDelay.delay()

            awsRetry.withRetry {
                log.info { "###### step2 람다 코드 업데이트 from ${jarFile.absolutePath} (${jarFile.length() / 1024 / 1024}mb)" }
                aws.lambda.updateFunctionCode(functionName, jarFile)
            }

            alias?.let {
                log.info { " -> 업데이트된 코드가 람다에 반영되는것을  ${lambdaLayerApplyDelay}동안 잠시 대기.." }
                lambdaCodeApplyDelay.delay()

                awsRetry.withRetry {
                    try {
                        val timeStart = TimeStart()
                        log.info { "###### step3 람다 버전 & alias 업데이트" }
                        val updatedVersion = aws.lambda.publishVersionAndUpdateAlias(functionName, it) //초기화시 오류나면 여기서 에러남
                        log.info { " -> 람다 버전 & alias 업데이트 완료 -> (${updatedVersion}) $timeStart" }
                    } catch (e: ResourceConflictException) {
                        throw e
                    } catch (e: Throwable) {
                        log.error { "###### 스냅스타트에서 오류가 발생했습니다. 로그 확인해주세요" }
                        throw e
                    }
                }
            }
        }
    }

    companion object {
        private val log = TempLogger(Level.DEBUG)
    }

}
