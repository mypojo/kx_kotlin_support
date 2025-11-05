package net.kotlinx.aws.lambda.dispatch.synch

import kotlinx.coroutines.runBlocking
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.okhttp.await
import okhttp3.OkHttpClient

/**
 * 배드락 메이전트 콜 호출을 실제 API 서버로 연결해준다
 * ex) 배드락 액선그룹 -> 람다 -> ECS
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
                        if (p["type"].str == "object") {
                            //컨트롤러에서 GET인데도 객체 타입으로 매핑하는경우, 스프링에서 인식하는 방식으로 펼쳐줘야함
                            //ex) "value": "{\"groupName\": \"관리자권한테스트\", \"sqlName\": \"전체미디어종합\", \"startDate\": \"20251101\", \"endDate\": \"20251130\"}"
                            //잘 안될 수 있으니 복잡한 파라메터의 경우 걍 POST로 전송받자
                            p["value"].str!!.toGsonData().entryMap().forEach { e ->
                                addQueryParameter(e.key, e.value.str!!)
                            }
                        } else {
                            addQueryParameter(p["name"].str!!, p["value"].str)
                        }
                    }
                }
                req.requestBody.lett { body = it } //body는 있으면 입력
            }
            resp.respText.toGsonData()
        }

    }


}