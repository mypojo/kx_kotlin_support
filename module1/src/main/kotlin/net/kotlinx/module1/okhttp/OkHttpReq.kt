package net.kotlinx.module1.okhttp

import net.kotlinx.module1.okhttp.OkHttpUtil.MEDIA_TYPE_IMAGE
import net.kotlinx.module1.okhttp.OkHttpUtil.MEDIA_TYPE_JSON
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/** 직렬화 가능한 요청 객체 : 응답이 텍스트인경우 */
open class OkHttpReq(
    val url: String,
    /** enum 안씀 */
    val method: String = "GET",
    var mediaType: MediaType = MEDIA_TYPE_JSON,
) {

    var body: String? = null
    var header: Map<String, String> = emptyMap()

    fun build(): Request {
        val builder = Request.Builder()
            .url(url)
            .method(method, body?.toRequestBody(mediaType))
        header.forEach { (k, v) -> builder.addHeader(k, v) }
        return builder.build()
    }

    /** 간단호출. */
    fun synchExe(client: OkHttpClient): OkHttpResp = client.newCall(this.build()).execute().use { OkHttpResp(it, it.body.string()) }
}


/** 응답이 스트림(파일 다운로드)인 경우 */
class OkHttpReqFile(
    url: String,
    /** 여기에 다운로드 */
    val downloadFile: File,
    mediaType: MediaType = MEDIA_TYPE_IMAGE,
    method: String = "GET"
) : OkHttpReq(url, method, mediaType) {

    /** 캐시된 이미지 등을 가져올때 사용함. 캐시 히트시 304 를 리턴함  */
    fun dirtyCheck(lastModified: String) {
        header = header + mapOf(
            OkHttpUtil.HRADER_IF_MODIFIED to lastModified,
        )
    }

    fun synchDownload(client: OkHttpClient): OkHttpResp {
        return client.newCall(this.build()).execute().use { response ->
            //파일 다운로드
            if (response.code == 200) {
                response.body.let {
                    it.byteStream().apply {
                        downloadFile.outputStream().use<FileOutputStream, Unit> { fileOut ->
                            copyTo(fileOut, OkHttpUtil.BUFFER_SIZE)
                        }
                    }
                }
            }
            OkHttpResp(response)
        }
    }
}
