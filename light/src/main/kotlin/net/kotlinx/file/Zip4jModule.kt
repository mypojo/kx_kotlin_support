package net.kotlinx.file

import net.kotlinx.core.Kdsl
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File


/**
 * 압축파일 읽고 쓰기
 */
class Zip4jModule {

    @Kdsl
    constructor(block: Zip4jModule.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 공통입력 ======================================================
    /** 비밀번호 */
    var password: String? = null

    /** zip결과 or unzip할 대상 */
    lateinit var targetZipFile: File

    //==================================================== 압축 ======================================================
    /** 압축할 파일들 */
    lateinit var files: List<File>

    /** 압축 */
    fun zip() {
        val zipParameters = ZipParameters()
        val zipFile: ZipFile = when (password) {
            null -> ZipFile(targetZipFile)
            else -> {
                zipParameters.isEncryptFiles = true
                zipParameters.encryptionMethod = EncryptionMethod.AES
                ZipFile(targetZipFile, password!!.toCharArray())
            }
        }
        files.forEach { file ->
            if (file.isFile) {
                zipFile.addFile(file, zipParameters)
            } else if ((file.isDirectory)) {
                zipFile.addFolder(file, zipParameters)
            }
        }
    }

    /** 대상 경로 그대로 압축을 푼다 */
    fun unzip() {
        unzip(targetZipFile.parentFile)
    }

    /** 압축 풀기~ */
    fun unzip(unzipDir: File) {
        check(unzipDir.isDirectory)
        val zipFile = ZipFile(targetZipFile)
        if (zipFile.isEncrypted) {
            zipFile.setPassword(password!!.toCharArray())
        }
        zipFile.extractAll(unzipDir.absolutePath)
    }

}