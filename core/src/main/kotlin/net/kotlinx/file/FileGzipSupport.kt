package net.kotlinx.file

import java.io.File

/** 단일 파일 압축 */
fun File.gzip(): File = FileGzipUtil.gzip(this)

fun File.unGzip(): File = FileGzipUtil.unGzip(this)