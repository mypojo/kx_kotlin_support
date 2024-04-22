package net.kotlinx.aws.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.getParameter
import aws.sdk.kotlin.services.ssm.model.ParameterNotFound
import java.io.File


/**
 * 단축메소드.
 * operator 사용안됨
 * @return 없으면 null 리턴
 *  */
suspend fun SsmClient.find(key: String): String? {
    return try {
        this.getParameter {
            this.name = key
            this.withDecryption = true
        }.parameter?.value
    } catch (e: ParameterNotFound) {
        null
    }
}


/**
 * 찾아서 스토어 대신 파일로 저장
 * ex) Google  키 저장 등등
 * @param file 저장할 파일. 이미 있으면 스킵함
 *  */
suspend fun SsmClient.findAndWrite(key: String, file: File) {

    if (file.exists()) return

    val value = this.find(key) ?: return

    file.writeText(value)
}