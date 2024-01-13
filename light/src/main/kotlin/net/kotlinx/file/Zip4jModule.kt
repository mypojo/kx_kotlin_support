package net.kotlinx.file

import net.kotlinx.core.Kdsl
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
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