package net.kotlinx.okhttp

import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object OkHttpUtil : KoinComponent {

    /**  베이스 버퍼 사이즈  */
    const val BUFFER_SIZE = 4096

    //==================================================== 헤더 ======================================================

    const val HRADER_LAST_MODIFIED = "Last-Modified"
    const val HRADER_IF_MODIFIED = "If-Modified-Since"

    /** 크롤링 기본 헤더 */
    val CRW = mapOf(
        "accept" to "*/*",
        "accept-Encoding" to "gzip, deflate, br",
        "accept-Language" to "ko,en;q=0.9,ko-KR;q=0.8,en-US;q=0.7",
        "cache-Control" to "no-cache",
        "pragma" to "no-cache",
        "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36",
        "upgrade-insecure-requests" to "1",
    )

    /** 일반적으로 사용되는 클라이언트 간단 생성 */
    fun client(timeout: Duration = 10.seconds, callTimeout: Duration = 60.seconds): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(1.seconds.toJavaDuration()) //1초 이내로 연결 안되면 문제 있는거
            .readTimeout(timeout.toJavaDuration()) //크롤링 경우 빠른 응답성을 위해서 2초 정도.
            .callTimeout(callTimeout.toJavaDuration()) //람다에서는 설정 주의!! 토탈 시간을 재는거라서 람다 등에서 처리가 늦으면 예외 던져짐
            .build()
    }

}
