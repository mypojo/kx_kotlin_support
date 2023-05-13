package net.kotlinx.aws1.okhttp

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume

/**
 * 코루틴을 지원해준다.
 * 서버리스 환경의 대량 크롤링 등에서 사용함
 * 참고 : https://github.com/gildor/kotlin-coroutines-okhttp
 * 리트라이는 각 벤더사 제품 쓸것. ex) AWS SFN
 */
suspend fun Call.await(): Response {
    return suspendCancellableCoroutine<Response> { continuation ->

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
    val resp = this.newCall(req.build()).await()
    return resp.use { OkHttpResp(req, it).load() }
}

