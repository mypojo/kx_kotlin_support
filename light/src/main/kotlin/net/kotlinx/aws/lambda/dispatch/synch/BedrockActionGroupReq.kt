package net.kotlinx.aws.lambda.dispatch.synch

import net.kotlinx.json.gson.GsonData

/**
 * 배드락 액션그룹 이벤트 데이터를 파싱하는 클래스
 * https://docs.aws.amazon.com/bedrock/latest/userguide/agents-lambda.html
 * @param body JSON 이벤트 데이터를 담은 GsonData 객체
 */
data class BedrockActionGroupReq(val body: GsonData) {

    /** 메시지 버전 정보 (예: "1.0") */
    val messageVersion: String = body["messageVersion"].str!!

    /** 사용자의 입력 텍스트 */
    val inputText: String = body["inputText"].str!!

    /** 세션 고유 식별자 */
    val sessionId: String = body["sessionId"].str!!

    /** 호출된 액션 그룹 이름 */
    val actionGroup: String = body["actionGroup"].str!!

    /** HTTP 메서드 (GET, POST 등) */
    val httpMethod: String = body["httpMethod"].str!!

    /** API 경로 (예: "/bizMoney") */
    val apiPath: String = body["apiPath"].str!!

    /**
     * 세션 속성 데이터
     * 사용자 ID, JWT 토큰 등의 인증/인가 정보를 포함
     */
    val sessionAttributes: GsonData = body["sessionAttributes"]

    /**
     * 프롬프트의 세션 속성 데이터
     *  */
    val promptSessionAttributes: GsonData = body["promptSessionAttributes"]

    /** 파라메터 어레이 */
    val parameters: GsonData = body["parameters"]

    /**
     * Bedrock Agent 정보를 담은 객체
     * 에이전트 이름, 버전, ID, 별칭 등을 포함
     */
    val agent: GsonData = body["agent"]

    /** 에이전트 이름 */
    val agentName: String = agent["name"].str!!

    /** 에이전트 버전 */
    val agentVersion: String = agent["version"].str!!

    /** 에이전트 고유 ID */
    val agentId: String = agent["id"].str!!

    /** 에이전트 별칭 */
    val agentAlias: String = agent["alias"].str!!

    /**
     * POST 등의 요청에 사용되는 body를 단일 obj 로 매핑한것 -> vo 변환이나 http body 구성에 사용
     * array는 적용 안되니 주의!!
     * */
    val requestBody: GsonData by lazy {
        val properties = this.body["requestBody"]["content"]["application/json;charset=UTF-8"]["properties"]
        GsonData.obj {
            properties.forEach { e ->
                put(e["name"].str!!, e["value"].str!!)
            }
        }
    }


}