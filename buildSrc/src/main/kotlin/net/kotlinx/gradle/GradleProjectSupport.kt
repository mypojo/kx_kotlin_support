package net.kotlinx.gradle

import net.kotlinx.system.OsType
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

/**
 * 커맨드라인 명령어 실행후 결과 리턴
 * */
fun Project.command(command: String): String {
    val commandList = OsType.OS_TYPE.toGradleCommand(command)
    val out = ByteArrayOutputStream()
    this.exec {
        commandLine(commandList)
        standardOutput = out
    }
    return out.toString()
}

//==================================================== 명령어 샘플 ======================================================

/** 현재 브랜치를 가져온다 */
fun Project.commandGitCurrentBrabch(): String = this.command("git rev-parse --abbrev-ref HEAD").replace("\n","")