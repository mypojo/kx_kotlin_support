package net.kotlinx.okhttp

import kotlinx.coroutines.suspendCancellableCoroutine
import net.kotlinx.io.output.toOutputResource
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume

/**
 * 코루틴을 지원해준다.
 * 서버리스 환경의 대량 크롤링 등에서 사용함
 * 참고 : https://github.com/gildor/kotlin-coroutines-okhttp
 * 리트라이는 각 벤더사 제품 쓸것. ex) AWS SFN
 */
suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->

        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                //첫 예외가 발생하면 즉시 중지
                continuation.cancel(e)
            }
        })

        continuation.invokeOnCancellation {
            //취소되면 여기 호출됨
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
    }
}

/** 비동기 호출 */
suspend fun OkHttpClient.await(block: OkHttpReq.() -> Unit): OkHttpResp {
    val req = OkHttpReq().apply(block)
    return await(req)
}

/** 비동기 호출 */
suspend fun OkHttpClient.await(req: OkHttpReq): OkHttpResp {
    this.reqInterceptor.invoke(req)
    val resp = this.newCall(req.build()).await()
    return resp.use { OkHttpResp(req, it).load() }
}

//==================================================== 다운로드 ======================================================

/** 단축 메소드 */
suspend fun OkHttpClient.download(url: String, file: File): OkHttpResp = download(OkHttpReq { this.url = url }) { file.toOutputResource().write(it) }

/** 단축 메소드 */
suspend fun OkHttpClient.download(url: String, block: (InputStream) -> Unit): OkHttpResp = download(OkHttpReq { this.url = url }, block)

/** 다운로드 */
suspend fun OkHttpClient.download(req: OkHttpReq, block: (InputStream) -> Unit): OkHttpResp {

    this.reqInterceptor.invoke(req)

    return this.newCall(req.build()).await().use { response ->
        if (!response.isSuccessful) {
            throw IOException("실패 응답코드 리턴! ${response.code} / ${response.message}")
        }
        val body = response.body ?: throw IOException("응답 본문이 비어 있습니다")
        body.byteStream().use { inputStream -> block(inputStream) }
        // 응답 객체 반환
        OkHttpResp(req, response)
    }
}