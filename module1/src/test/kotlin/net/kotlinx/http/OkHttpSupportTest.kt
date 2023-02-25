package net.kotlinx.http

import mu.KotlinLogging
import net.kotlinx.http.OkHttpSupportTest.OkHttpReq.Companion.BUFFER_SIZE
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream


internal class OkHttpSupportTest {

    private val log = KotlinLogging.logger {}

    private val client = OkHttpClient()

    /** 직렬화 가능한 요청 객체 (file제외) */
    data class OkHttpReq(
        val url: String,
        /** enum 안씀 */
        val method: String = "GET",
        val body: String? = null,
        val header: Map<String, String> = emptyMap(),
        val mediaType: MediaType = DEFAULT_MEDIA_TYPE,
        /** 파일이 있으면 다운로드로 간주. 아니라면 결과 객체에 결과 첨부 */
        val downloadFile: File? = null,
    ) {

        companion object {
            /** 디폴트 미디어타입 */
            val DEFAULT_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
            /**  베이스 버퍼 사이즈  */
            const val BUFFER_SIZE = 4096
        }
    }

    /**
     * 결과 스트림을 닫은 후 리턴하기 위해서 결과 객체를 따로 만든다.
     * httpLog 로 변환 가능
     * */
    data class OkHttpResp(
        /** 요청 객체 */
        val req: OkHttpReq,
        /** okhttp의 응답 객체*/
        val response: Response,
        /** 텍스트로 변환한 body */
        val respText: String? = null,
    )

    /** 간단호출. */
    fun OkHttpClient.synchExe(req: OkHttpReq): OkHttpResp {
        val request = Request.Builder()
            .url(req.url)
            .method(req.method, req.body?.toRequestBody(req.mediaType))
        req.header.forEach { (k, v) -> request.addHeader(k, v) }
        return newCall(request.build()).execute().use { response ->
            //2가지 케이스만 고려한다.
            if (req.downloadFile == null) {
                OkHttpResp(req, response, response.body?.string())
            } else {
                //파일 다운로드
                response.body?.let {
                    it.byteStream().apply {
                        req.downloadFile.outputStream().use<FileOutputStream, Unit> { fileOut ->
                            copyTo(fileOut, BUFFER_SIZE)
                        }
                    }
                }
                OkHttpResp(req, response)
            }
        }
    }

    @Test
    fun `기본테스트`() {
        val ok: OkHttpReq = OkHttpReq("https://publicobject.com/helloworld.txt")
        val resp = client.synchExe(ok)
        println(resp.respText)
    }



}