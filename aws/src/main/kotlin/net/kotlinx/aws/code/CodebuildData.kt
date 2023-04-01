package net.kotlinx.aws.code

import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core1.DeploymentType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 코드빌드용 설정데이터
 * 그래들 빌드파일에 사용됨 (그래들 의존성은 없음)
 *  */
data class CodebuildData(
    val awsId: String,
    val profileName: String,
    val region: String = "ap-northeast-2",
    /** 배포 타입 */
    val deploymentType: DeploymentType = DeploymentType.load(),
    /** 커밋정보 (환경변수에 미리 입력됨) */
    val commitId: String = System.getenv("COMMIT_ID") ?: "unknown"
) {

    //==================================================== 커맨드 ======================================================

    /** 윈도우 환경인지? (명령어셋 틀려짐) */
    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")

    /**
     * os에 따른 커멘드 라인을 리턴해줌
     * ex)  commandLine(build.command(command))
     * */
    fun command(command: String): List<String> = if (isWindows) listOf("cmd", "/c", command) else listOf("bash", "-c", command)

    /** 로컬 실행의 경우 프로파일을 추가로 입력해줘야함 */
    private val profileCmd = if (isWindows) "--profile $profileName" else ""

    //==================================================== ECR ======================================================
    /** ECR 주소 */
    fun ecrUrl(repositoryName: String) = "${awsId}.dkr.ecr.${region}.amazonaws.com/${repositoryName}"

    /** ECR 로그인 주소 */
    fun ecrLoginCommand(ecrUrl: String): String = "aws ecr get-login-password --region $region $profileCmd | docker login --username AWS --password-stdin $ecrUrl"

    /** 브랜치 명 (코드디플로이의 경우 환경변수에 넣어줌) */
    val branchName = System.getenv("BRANCH_NAME") ?: deploymentType.name

    /** 최종 태그 명 */
    val ecrTagName = "$branchName-${DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").withZone(ZoneId.of("Asia/Seoul")).format(LocalDateTime.now())}"

    //==================================================== AWS ======================================================

    /** 기본 클라이언트 */
    val aws by lazy { AwsConfig(profileName = profileName).toAwsClient() }

    /** 로깅용 문구 */
    override fun toString(): String = "isWindows($isWindows) / tagName($ecrTagName) / commitId($commitId)"

}