package net.kotlinx.string

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 텍스트를 암호화하는 단순 유틸
 * */
class EncryptDecryptTool(
    key: String,
    /** 키 길이 맞추기 (16, 24, 32 중 하나) */
    keySize: Int = 24,
    private val transformation: String = "AES/ECB/PKCS5Padding",
    private val algorithm: String = "AES",
) {

    private val keyBytes = key.toByteArray().copyOf(keySize)

    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation) //스래드 안전하지 않음으로 매번 호출
        val secretKey = SecretKeySpec(keyBytes, algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation)
        val secretKey = SecretKeySpec(keyBytes, algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decoded = Base64.getDecoder().decode(input)
        return String(cipher.doFinal(decoded))
    }

}