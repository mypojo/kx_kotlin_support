package net.kotlinx.file

import java.io.File
import java.nio.charset.Charset

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

/** 기존파일에 확장자는 그대로 두고 이름을 추가해준다. 은근 자주 사용됨 */
fun File.nameAppend(append: String): File = this.parentFile.slash("${this.nameWithoutExtension}${append}.${this.extension}")


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

/**
 * readLines 인데 앞의 특정 부분만 사용하고 싶을때
 * @see readLines
 * */
fun File.readLines(limit: Int, charset: Charset = Charsets.UTF_8): List<String> {
    val result = ArrayList<String>()
    forEachLine(charset) {
        if (result.size >= limit) return@forEachLine
        result.add(it)
    }
    return result
}