package net.kotlinx.okhttp

import net.kotlinx.core.Kdsl
import net.kotlinx.core.string.CharSets
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset

/** 직렬화 가능한 요청 객체 : 응답이 텍스트인경우 */
class OkHttpReq {

    /** url */
    lateinit var url: String

    /** enum 안씀 */
    var method: String = "GET"

    /** 미디어 */
    var mediaType: MediaType = OkHttpMediaType.JSON

    /** 헤더 */
    var header: Map<String, String> = emptyMap()

    /** post 등 */
    var body: Any? = null

    /**
     * 이게 null이 아닌경우 body를 toString() 할때 해당 인코딩으로 응답을 강제변환
     * 일반적으로 content-type 에 인코딩을 명시하지 않은애들 MS949로 바꿀때 사용
     *  */
    var defaultChsrSet: Charset? = CharSets.MS949

    /** 쿼리파라메터 등이 필요할때는 간단하게 이걸 사용 */
    @Kdsl
    fun url(path: String, block: HttpUrl.Builder.() -> Unit = {}) {
        url = path.toHttpUrl().newBuilder().apply(block).build().toString()
    }

    fun build(): Request {
        val builder = Request.Builder()
            .url(url)
            .method(method, body?.toString()?.toRequestBody(mediaType))
        header.forEach { (k, v) -> builder.addHeader(k, v) }
        return builder.build()
    }

    //==================================================== 편의용 메소드 ======================================================

    /** 캐시된 이미지 등을 가져올때 사용함. 캐시 히트시 304 를 리턴함  */
    fun dirtyCheck(lastModified: String) {
        header = header + mapOf(
            OkHttpUtil.HRADER_IF_MODIFIED to lastModified,
        )
    }

}

