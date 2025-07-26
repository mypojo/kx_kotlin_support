package net.kotlinx.io.output

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.GZIPOutputStream


/**
 * InputResource 파일용
 * */
data class OutputFileResource(
    /** 파일 */
    val file: File,
    /** 파일로 쓸 경우 압축 여부 */
    val writerGzip: Boolean = false
) : OutputResource {

    override val outputStream: OutputStream
        get() {
            val fileOut = FileOutputStream(file)
            return if (writerGzip) GZIPOutputStream(fileOut) else fileOut
        }


}