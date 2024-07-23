package net.kotlinx.file

import java.io.File

/**
 * 파일을 패스 형식으로 나타낼때
 * ex) ResourceHolder.WORKSPACE.slash("work").slash("kotlin.html")
 *  */
fun File.slash(name: String): File {
    this.mkdirs()
    check(this.isDirectory) { "이 파일은 디렉토리가 아닙니다. $this" }
    return File(this, name)
}

/** 셀프 리턴. apply 한줄 줄이려고 추가함 */
fun File.slashDir(name: String): File = slash(name).apply { this.mkdirs() }


/** 해당 디렉토리의 모든 파일 가져옴 */
fun File.listAllFiles(): List<File> {
    check(this.isDirectory) { "이 파일은 디렉토리가 아닙니다. $this" }
    val files = mutableListOf<File>()
    for (file in this.listFiles()) {
        if (file.isFile) {
            files.add(file)
        } else if (file.isDirectory) {
            files.addAll(file.listAllFiles())
        }
    }
    return files
}
