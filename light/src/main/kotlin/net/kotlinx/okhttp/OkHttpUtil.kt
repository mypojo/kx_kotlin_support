package net.kotlinx.okhttp

import net.kotlinx.core.regex.RegexSet
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object OkHttpUtil : KoinComponent {

    /** 디폴트 미디어타입 */
    val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    /** 미디어타입 이미지 */
    val MEDIA_TYPE_IMAGE = "image/jpeg".toMediaType()

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


    /** 아웃바운드 IP를 간단히 리턴해준다.  */
    fun findOutboundIp(): String {
        val client: OkHttpClient by inject()
        val resp: String = client.fetch {
            url = "https://www.findip.kr/"
        }.respText

        return RegexSet.extract("(IP Address): ", "</h2>").toRegex().find(resp)!!.value
    }

    /** 일반적으로 사용되는 클라이언트 간단 생성 */
    fun client(timeout: Duration = 10.seconds, callTimeout: Duration = 60.seconds): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(1.seconds.toJavaDuration()) //1초 이내로 연결 안되면 문제 있는거
            .readTimeout(timeout.toJavaDuration()) //크롤링 경우 빠른 응답성을 위해서 2초 정도.
            .callTimeout(callTimeout.toJavaDuration()) //람다에서는 설정 주의!! 토탈 시간을 재는거라서 람다 등에서 처리가 늦으면 예외 던져짐
            .build()
    }

}
