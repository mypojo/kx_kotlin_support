package net.kotlinx.core.file

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream


/** 단일 파일 압축 */
fun File.gzip(): File = FileGzipUtil.gzip(this)

/**
 * 스트림 압축은 GzipResourceSupport 참고
 * */
object FileGzipUtil {

    /** 한개 파일을 한개의 gzip 으로 압축한다. athena 등에서 csv 를 업로드 할때 사용함  */
    fun gzip(file: File): File {
        return File(file.absolutePath + ".gz").also { zipFile ->
            GZIPOutputStream(FileOutputStream(zipFile)).use { gos ->
                FileInputStream(zipFile).use { fis ->
                    // copy zipFile
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (fis.read(buffer).also { len = it } > 0) {
                        gos.write(buffer, 0, len)
                    }
                }
            }
        }
    }

}

