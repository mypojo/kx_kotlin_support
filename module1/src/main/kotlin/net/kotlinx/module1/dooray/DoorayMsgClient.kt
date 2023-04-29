package net.kotlinx.module1.dooray

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.module1.okhttp.fetch
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 아이콘과 타이틀당 1개씩 만들어 사용하면 됨
 * ex) 에러 전용용, 빌드 알람용 등등
 *
 * 두레이측에서는 약 1초 이내 여러개 보내면 무시해버리고 실제 전송이 되었는지 알려주지 않음.
 * 문의 결과 의도된 거라고 함.. 장난??
 */
class DoorayMsgClient(
    /** 채팅방 -> 우상단 정보 -> 서비스 연동 -> 인커밍 훅 에서 복붙 */
    val roomUri: String,
    /** 기본 이름 */
    val name: String = "두레이 봇",
    /** 기본 아이콘 */
    val icon: String = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
) : KoinComponent {

    private val client: OkHttpClient by inject()

    private val log = KotlinLogging.logger {}

    /** 직접 전송  */
    fun sendDirect(message: String) {

        val resp = client.fetch {
            url = roomUri
            method = "POST"
            body = obj {
                "botName" to name
                "botIconImage" to icon
                "text" to message
            }
        }

        if (resp.ok) {
            log.debug { " -> 응답성공 : ${resp.respText}" }
        } else {
            log.warn { " -> 응답실패 : ${resp.response.code} / ${resp.respText}" }
        }
    }

}
