package net.kotlinx.core2.file

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream


object FileGzipUtil {

    /** 한개 파일을 한개의 gzip 으로 압축한다. athena 등에서 csv 를 업로드 할때 사용함  */
    fun gzip(file: File): File {
        val zip = File(file.absolutePath + ".gz")
        GZIPOutputStream(FileOutputStream(zip)).use { gos ->
            FileInputStream(file).use { fis ->
                // copy file
                val buffer = ByteArray(1024)
                var len: Int
                while (fis.read(buffer).also { len = it } > 0) {
                    gos.write(buffer, 0, len)
                }
            }
        }
        return zip
    }

}

