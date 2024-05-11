package net.kotlinx.google.recaptchar

import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.okhttp.fetch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

/**
 * https://www.google.com/recaptcha
 * 써보고 타임아웃이 필요하면?
 */
class RecaptchaModule(
    private val okHttpClient: OkHttpClient,
    private val secretKey: String
) {

    private val log = KotlinLogging.logger {}

    fun isValid(response: String, remoteip: String? = null): Boolean {

        val resp = okHttpClient.fetch {
            url = VERIFY_URL.toHttpUrl().newBuilder().apply {
                addQueryParameter("response", response)
                addQueryParameter("secret", secretKey)
                remoteip?.let { addQueryParameter("remoteip", it) }
            }.build().toString()
        }

        val result = GsonData.parse(resp.respText)
        val success = result["success"].bool ?: false
        if (!success) {
            for (each in result["error-codes"]) {
                log.debug { "[${remoteip}]로 부터의 캡챠코드 검증실패 : $each" }
            }
        }
        return success
    }

    companion object {
        private const val VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify"
    }
}
