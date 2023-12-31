package net.kotlinx.core.file

import java.io.File

/**
 * 파일을 패스 형식으로 나타낼때
 * ex) ResourceHolder.getWorkspace().slash("work").slash("kotlin.html")
 *  */
fun File.slash(name: String): File {
    this.mkdirs()
    check(this.isDirectory) { "이 파일은 디렉토리가 아닙니다. $this" }
    return File(this, name)
}