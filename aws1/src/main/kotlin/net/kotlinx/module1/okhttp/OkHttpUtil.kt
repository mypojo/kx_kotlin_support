package net.kotlinx.module1.okhttp

import net.kotlinx.core1.regex.RegexSet
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

object OkHttpUtil {

    /** 디폴트 미디어타입 */
    val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    /** 미디어타입 이미지 */
    val MEDIA_TYPE_IMAGE = "image/jpeg".toMediaType()

    /**  베이스 버퍼 사이즈  */
    const val BUFFER_SIZE = 4096

    //==================================================== 헤더 ======================================================

    const val HRADER_LAST_MODIFIED = "Last-Modified"
    const val HRADER_IF_MODIFIED = "If-Modified-Since"

    /** 아웃바운드 IP를 간단히 리턴해준다.  */
    fun findOutboundIp(client: OkHttpClient = OkHttpClient()): String {

        val resp: String = client.fetch {
            url = "https://www.findip.kr/"
        }.respText!!

        return RegexSet.between("(IP Address): ", "</h2>").find(resp)!!.value
    }

}
