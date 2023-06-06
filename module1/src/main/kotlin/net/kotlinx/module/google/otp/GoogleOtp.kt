package net.kotlinx.module.google.otp

import org.apache.commons.codec.binary.Base32
import java.security.SecureRandom
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * 현재시간 베이스로 작동되는 구글 OTP
 * 관리자 로그인 등에서 사용됨
 * */
object GoogleOtp {

    /**
     * 이 결과값은 DB에 함께 보관할것
     * 수동 등록하면 잘 됨
     *  */
    fun generateSecretKey(keySize: Int = 20): String {
        return ByteArray(keySize).let {
            SecureRandom().nextBytes(it)
            Base32().encodeToString(it)
        }
    }

    fun checkCode(secretKey: String, otpCode: Long): Boolean {
        val wave: Long = Date().time / 30000
        var result = false
        val codec = Base32()
        val decodedKey = codec.decode(secretKey)
        val window = 3
        for (i in -window..window) {
            val hash: Long = verifyCode(decodedKey, wave + i)
            if (hash == otpCode) result = true
        }
        return result
    }

    /** 주워옴 */
    private fun verifyCode(key: ByteArray, t: Long): Long {
        val data = ByteArray(8)
        var value = t
        var i = 8
        while (i-- > 0) {
            data[i] = value.toByte()
            value = value ushr 8
        }
        val signKey = SecretKeySpec(key, "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(signKey)
        val hash = mac.doFinal(data)
        val offset = hash[20 - 1].toInt() and 0xF
        var truncatedHash: Long = 0
        for (i in 0..3) {
            truncatedHash = truncatedHash shl 8
            truncatedHash = truncatedHash or (hash[offset + i].toInt() and 0xFF).toLong()
        }
        truncatedHash = truncatedHash and 0x7FFFFFFFL
        truncatedHash %= 1000000
        return truncatedHash
    }


}
