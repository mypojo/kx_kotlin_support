package net.kotlinx.okhttp

import mu.KotlinLogging
import okhttp3.Response
import okio.GzipSource
import okio.buffer

/**
 * 결과 스트림을 닫은 후 리턴하기 위해서 결과 객체를 따로 만든다.
 * httpLog 로 변환 가능
 * */
data class OkHttpResp(
    /** 요청 객체 */
    val okHttpReq: OkHttpReq,
    /** okhttp의 응답 객체*/
    val response: Response,
) {

    companion object {

        private val log = KotlinLogging.logger {}
    }

    /** resp 스트림을 가져온다.  IO 작업이라서 별도 호출 */
    fun load(): OkHttpResp {
        //gzip이면 풀어준다
        respText = if (response.header("Content-Encoding") == "gzip") {
            GzipSource(response.body!!.source()).buffer().readUtf8()
        } else {
            val contentType = response.headers["content-type"]
            val charSetConfig = contentType?.contains("charset", false) == true
            when {
                // 하지만 응답 헤더와는 다르게 작업되는 사이트의 경우 강제 입력 ex) text/html 이렇게만 덜렁 있음..
                okHttpReq.defaultChsrSet != null && !charSetConfig -> {
                    log.trace { " => 컨텐츠 타입($contentType)에 인코딩 설정이 없음 -> 기본 인코딩(${okHttpReq.defaultChsrSet}) 강제 적용" }
                    response.body!!.source().readString(okHttpReq.defaultChsrSet!!)
                }

                else -> response.body!!.string() //그냥 이걸로 변환시 응답 헤더를 보고 잘 변환됨
            }

        }
        return this
    }

    /** 텍스트로 변환한 body */
    lateinit var respText: String

    /** 200 code일때만 */
    val ok: Boolean
        get() = response.code == 200

    /** 이미지 등의 캐시에 사용됨 */
    val lastModified: String by lazy { response.headers[OkHttpUtil.HRADER_LAST_MODIFIED] ?: "" }

}