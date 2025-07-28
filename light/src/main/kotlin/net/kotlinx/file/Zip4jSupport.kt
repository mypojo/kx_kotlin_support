package net.kotlinx.file

import java.io.File

/** 단일 파일 압축 */
fun File.zip4j(password: String? = null): File {
    val file = this
    val ziped = Zip4jModule {
        this.files = listOf(file)
        this.password = password
        zip()
    }
    return ziped.targetZipFile
}

/** 단일 파일 압축해제 */
fun File.unzip4j(password: String? = null): File {
    val file = this
    val ziped = Zip4jModule {
        this.targetZipFile = file
        this.password = password
        unzip()
    }
    return ziped.targetZipFile.parentFile
}