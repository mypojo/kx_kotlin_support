package net.kotlinx.file

import java.io.File
import java.nio.charset.Charset


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
 * readLines 인데 앞의 특정 부분만 인라인으로 사용하고 싶을때
 * @see readLines
 * */
@Deprecated("flow를 사용하세요")
fun File.readLines(limit: Int, charset: Charset = Charsets.UTF_8): List<String> {
    val result = ArrayList<String>()
    this.forEachLine(charset) {
        if (result.size >= limit) return@forEachLine
        result.add(it)
    }
    return result
}