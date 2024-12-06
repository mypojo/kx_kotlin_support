package net.kotlinx.file

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


/** 단일 파일 압축 */
fun File.gzip(): File = FileGzipUtil.gzip(this)

/**
 * 스트림 압축은 GzipResourceSupport 참고
 * */
object FileGzipUtil {

    private const val SUFF = ".gz"

    /** 한개 파일을 한개의 gzip 으로 압축한다. athena 등에서 csv 를 업로드 할때 사용함  */
    fun gzip(file: File): File {
        return File(file.absolutePath + SUFF).also { zipFile ->
            GZIPOutputStream(FileOutputStream(zipFile)).use { gos ->
                FileInputStream(file).use { fis ->
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

    /** 한개 대상으로만 가능  */
    fun unGzip(gzipFile: File): File {
        val unzipFile = gzipFile.parentFile.slash(gzipFile.name.removeSuffix(SUFF))
        FileInputStream(gzipFile).use { fis ->
            GZIPInputStream(fis).use { gis ->
                FileOutputStream(unzipFile).use { fos ->
                    BufferedOutputStream(fos).use { bos ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (gis.read(buffer).also { len = it } > 0) {
                            bos.write(buffer, 0, len)
                        }
                    }
                }
            }
        }
        return unzipFile
    }
}

