package net.kotlinx.gradle

import net.kotlinx.aws.AwsCommandUtil
import net.kotlinx.file.slash
import net.kotlinx.system.OsType
import org.gradle.api.Project
import java.io.File

/**
 * 커맨드라인 명령어 실행후 결과 리턴
 * */
fun command(command: String, dir: File? = null): String {
    val commandList = OsType.OS_TYPE.command() + listOf(command)

    val processBuilder = ProcessBuilder(commandList)
    dir?.let { processBuilder.directory(it) }

    val process = processBuilder.start()
    val result = process.inputStream.bufferedReader().use { it.readText() }
    process.waitFor()
    return result
}

/**
 * vite 용 cmd 접두어
 * */
val cmdSuff = if (OsType.OS_TYPE == OsType.WINDOWS) ".cmd" else ""
//==================================================== 명령어 샘플 ======================================================

/** 현재 브랜치를 가져온다 */
fun Project.commandGitCurrentBrabch(): String = command("git rev-parse --abbrev-ref HEAD").replace("\n", "").trim()

/**
 * S3를 동기화한다
 * 원격지의 파일을 삭제하지는 않는다.
 *  */
fun Project.commandS3Synch(sourceDirName: String, bucketName: String, profile: String? = null) {
    val profileCmd = AwsCommandUtil.profile(profile)
    val sourceDir = projectDir.slash(sourceDirName)
    command("aws s3 sync $sourceDir s3://${bucketName} $profileCmd")
}