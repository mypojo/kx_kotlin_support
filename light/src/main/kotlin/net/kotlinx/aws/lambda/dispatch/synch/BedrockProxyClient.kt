package net.kotlinx.aws.lambda.dispatch.synch

import kotlinx.coroutines.runBlocking
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.okhttp.await
import okhttp3.OkHttpClient

/**
 * 배드락 메이전트 콜 호출을 실제 API 서버로 연결해준다
 * MCP 대용으로 사용
 * */
class BedrockProxyClient(

    /** 호스트 서버 주소 */
    val endpoint: String,

    /** 기본 헤더 */
    val defaultHeaderMap: Map<String, String> = mapOf(
        "Content-Type" to "application/json"
    )

) {

    /** http 클라이언트 */
    private val http by koinLazy<OkHttpClient>()

    fun invoke(req: BedrockActionGroupReq, headerMap: Map<String, String>): GsonData {
        return runBlocking {
            val fullPath = endpoint + req.apiPath
            val resp = http.await {
                method = req.httpMethod
                header = defaultHeaderMap + headerMap
                url(fullPath) {
                    //쿼리스트링 입력
                    req.parameters.forEach { p ->
                        addQueryParameter(p["name"].str!!, p["value"].str)
                    }
                }
                req.requestBody.lett { body = it } //body는 있으면 입력
            }
            resp.respText.toGsonData()
        }

    }


}