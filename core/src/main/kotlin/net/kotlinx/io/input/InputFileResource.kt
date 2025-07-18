package net.kotlinx.io.input

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream


/**
 * InputResource 파일용
 * */
data class InputFileResource(
    /** 파일 */
    val file: File,
    /** 파일을 읽을 때 압축 해제 여부 */
    var readerGzip: Boolean = false

) : InputResource {

    override val inputStream: InputStream
        get() {
            val fileIn = FileInputStream(file)
            return if (readerGzip) GZIPInputStream(fileIn) else fileIn
        }


}