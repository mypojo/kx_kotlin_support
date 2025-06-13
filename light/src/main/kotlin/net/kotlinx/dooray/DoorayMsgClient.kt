package net.kotlinx.dooray

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins.koin
import net.kotlinx.okhttp.fetch
import okhttp3.OkHttpClient

/**
 * 아이콘과 타이틀당 1개씩 만들어 사용하면 됨
 * ex) 에러 전용용, 빌드 알람용 등등
 *
 * 두레이측에서는 약 1초 이내 여러개 보내면 무시해버리고 실제 전송이 되었는지 알려주지 않음.
 * 문의 결과 의도된 거라고 함.. 장난??
 *
 * 메세지 꾸미기 참고
 * https://helpdesk.dooray.com/share/pages/9wWo-xwiR66BO5LGshgVTg/2900079844453730084
 */
class DoorayMsgClient {

    @Kdsl
    constructor(block: DoorayMsgClient.() -> Unit = {}) {
        apply(block)
    }

    private val log = KotlinLogging.logger {}

    /** http 클라이언트 */
    private val client: OkHttpClient = koin<OkHttpClient>()

    /** 채팅방 -> 우상단 정보 -> 서비스 연동 -> 인커밍 훅 에서 복붙 */
    lateinit var roomUri: String

    /** 기본 이름 */
    var name: String = "두레이 봇"

    /** 기본 아이콘 */
    var icon: String = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png"

    /**
     * 직접 전송. 라인분리 안됨!!!
     * 대량전송이 안되기때문에 fetch 사용함
     *  */
    fun sendDirect(message: String, roomUri: String = this.roomUri) {

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