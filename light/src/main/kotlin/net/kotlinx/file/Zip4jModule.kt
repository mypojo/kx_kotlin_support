package net.kotlinx.file

import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File


/**
 * 압축파일 읽고 쓰기
 * 바닐라 zip 기능에서 각종 부가기능이 추가됨
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

    /** 분할압축 */
    var splitSizeMb: Long? = null

    /** 압축 */
    fun zip() {

        //targetZipFile 생략 가능!
        if (!this::targetZipFile.isInitialized) {
            check(files.size == 1) { "zip 결과파일 네임은 압축대상이 1개 일때만 생략 가능합니다." }
            targetZipFile = File(files.first().absolutePath + ".zip")
        }

        val zipParameters = ZipParameters()

        val zipFile: ZipFile = when (password) {
            null -> ZipFile(targetZipFile)
            else -> {
                zipParameters.isEncryptFiles = true
                zipParameters.encryptionMethod = EncryptionMethod.AES
                ZipFile(targetZipFile, password!!.toCharArray())
            }
        }
        if (splitSizeMb != null) {
            log.trace { "분할 압축을 시도합니다.." }
            zipParameters.compressionMethod = CompressionMethod.DEFLATE
            zipFile.createSplitZipFile(files, zipParameters, true, splitSizeMb!! * 1024 * 1024)
        } else {
            log.trace { "일반 압축을 시도합니다.." }
            files.forEach { file ->
                if (file.isFile) {
                    zipFile.addFile(file, zipParameters)
                } else if (file.isDirectory) {
                    zipFile.addFolder(file, zipParameters)
                }
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

    companion object {
        private val log = KotlinLogging.logger {}
    }

}