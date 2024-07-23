package net.kotlinx.okhttp

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File


private val OKHTTP_REQ_INTERCEPTOR: MutableMap<OkHttpClient, (OkHttpReq) -> Unit> = mutableMapOf()

/** interceptor 설정 추가 */
var OkHttpClient.reqInterceptor: (OkHttpReq) -> Unit
    get() = OKHTTP_REQ_INTERCEPTOR.getOrDefault(this) {}
    set(value) {
        OKHTTP_REQ_INTERCEPTOR[this] = value
    }

/**
 * 멀티파트 파일 업로드 샘플
 * https://httpbin.org/post 로 테스트 가능
 * 사용금지!!  가능하면 프리사인 사용하세요!!
 *  */
fun OkHttpClient.fileUploadSample(url: String, file: File, block: MultipartBody.Builder.() -> Unit = {}): Response {
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, file.asRequestBody(OkHttpMediaType.STREAM))
        //.addFormDataPart("", "")
        .apply(block)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    return this.newCall(request).execute()
}
