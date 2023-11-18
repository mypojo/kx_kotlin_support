package net.kotlinx.gradle

import aws.sdk.kotlin.services.lambda.model.ResourceConflictException
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.lambda.*
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.concurrent.delay
import net.kotlinx.core.retry.RetryTemplate
import net.kotlinx.core.time.TimeStart
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

/**
 * 코드빌드용 설정데이터
 * 그래들 빌드파일에 사용됨 (그래들 의존성은 없음)
 *  */
class GradleBuilder(
    val awsId: String,
    val profileName: String? = null,
    block: GradleBuilder.() -> Unit = {}
) {

    /** 리즌정보 */
    var region: String = AwsConfig.SEOUL

    /** 배포 타입 */
    var deploymentType: DeploymentType = DeploymentType.load()

    /** 커밋정보 (환경변수에 미리 입력됨) */
    var commitId: String = System.getenv("COMMIT_ID") ?: "unknown"

    //==================================================== 커맨드 ======================================================

    /** 윈도우 환경인지? (명령어셋 틀려짐) */
    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")

    /**
     * os에 따른 커멘드 라인을 리턴해줌
     * ex)  commandLine(build.command(command))
     * 일반 js build 명령은 cmd 등이 앞에 있어야 하고 AWS 호출은 cmd 없어도 됨..
     * 한번에 한개의 커맨드만 exec{} 안에 둘것!
     * js 번들링의 경우 각 플젝 루트에서 실행하면 됨
     * ex) npx vite build
     * ex) aws s3 sync ${project(":demo-svelte").projectDir}\dist s3://demo.kotlinx.net/
     * */
    fun command(command: String): List<String> = if (isWindows) listOf("cmd", "/c", command) else listOf("bash", "-c", command)

    /** 로컬 실행의 경우 프로파일을 추가로 입력해줘야함 */
    val profileCmd = if (isWindows && profileName != null) "--profile $profileName" else ""

    //==================================================== ECR ======================================================
    /** ECR 주소 */
    fun ecrUrl(repositoryName: String) = "${awsId}.dkr.ecr.${region}.amazonaws.com/${repositoryName}"

    /** ECR 로그인 주소 */
    fun ecrLoginCommand(ecrUrl: String): String = "aws ecr get-login-password --region $region $profileCmd | docker login --username AWS --password-stdin $ecrUrl"

    /**
     * 브랜치 명 (최종 벨리데이션 체크에 사용)
     * 코드디플로이 -> 경우 환경변수에 넣어줌
     * 로컬 ->  git branch
     * */
    val branchName: String? = System.getenv("BRANCH_NAME")

    /** 최종 태그 명 */
    val ecrTagName = "$deploymentType-${DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").withZone(ZoneId.of("Asia/Seoul")).format(LocalDateTime.now())}"

    //==================================================== AWS ======================================================

    val awsConfig = AwsConfig(profileName = profileName)

    /** 기본 클라이언트 */
    val aws1 by lazy { awsConfig.toAwsClient1() }

    /** 로깅용 문구 */
    override fun toString(): String = "isWindows($isWindows) / tagName($ecrTagName) / commitId($commitId)"


    //==================================================== 이하 람다 간단 배포 ======================================================

    /** 기본 접미어 */
    val suff = deploymentType.name.lowercase()

    /** 업로드 버킷 */
    var codeBucket: String = "${profileName}-work-${suff}"

    /** 업로드 키 접미어 */
    var codePath = "code"

    /** 레이어 리스트 */
    lateinit var layers: List<String>

    /** 레이어배포의 경우 단계별로 진행해야 해서 여기 단축함수를 넣음 */
    fun updateLayer(layerName: String, zipFile: File) {
        runBlocking {
            val start = TimeStart()
            println("###### 레이어 파일(${zipFile.length() / 1024 / 1024}mb)을 업로드합니다... ")
            aws1.s3.putObject(codeBucket, "${codePath}/${layerName}/${zipFile.name}", zipFile)
            println("###### 레이어 파일(${zipFile.length() / 1024 / 1024}mb)을 업로드 완료 -> $start")

            val layerArn = aws1.lambda.publishLayerVersion(codeBucket, "code/${layerName}/${zipFile.name}", layerName)
            println("###### 레이어를 업데이트 완료 -> $layerArn")
        }
    }

    val awsRetry: RetryTemplate = RetryTemplate {
        interval = 3.seconds
        predicate = RetryTemplate.match(ResourceConflictException::class.java)
    }

    /** 람다배포의 경우 단계별로 진행해야 해서 여기 단축함수를 넣음 */
    fun updateLambda(functionName: String, jarFile: File, alias: String? = null) {
        runBlocking {
            val layerArns = aws1.lambda.listLayerVersions(layers).map { it.layerVersionArn!! }
            aws1.lambda.updateFunctionLayers(functionName, layerArns)
            println("###### 람다[${functionName}] = 레이어 최신버전으로 업데이트")
            layerArns.forEach { println(" -> $it") }

            2.seconds.delay() //코드반영까지 잠시 대기
            awsRetry.withRetry {
                aws1.lambda.updateFunctionCode(functionName, jarFile)
                println("###### 람다 코드 업데이트 from ${jarFile.absolutePath} (${jarFile.length() / 1024 / 1024}mb)")
            }

            alias?.let {
                3.seconds.delay() //코드반영까지 잠시 대기
                try {
                    val updatedVersion = aws1.lambda.publishVersionAndUpdateAlias(functionName, it) //초기화시 오류나면 여기서 에러남
                    println("###### 람다 버전 업데이트 -> (${updatedVersion})")
                } catch (e: Throwable) {
                    println("###### 스냅스타트에서 오류가 발생했습니다. 로그 확인해주세요")
                    throw e
                }
            }
        }
    }

    init {
        block(this)
    }

}