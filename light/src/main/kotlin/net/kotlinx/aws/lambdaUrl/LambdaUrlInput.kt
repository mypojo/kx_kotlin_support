package net.kotlinx.aws.lambdaUrl

import net.kotlinx.core.gson.GsonData

/**
 * https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html
 * 람다 URL 직접호출 -> v2로 넘어옴
 * API gateway rest 호출 -> v1으로 넘어오는듯
 * 참고로 context 에는 awsRequestId, logStreamName 등이 입력됨 -> 안씀
 *  */
class LambdaUrlInput(
    val input: GsonData
) {

    /**
     * 경로. ex) /aa/bb
     * 1.0 버전도 인식 가능하도록 구성
     * */
    val path: String = extractPath(input)!!


    /** 자동 변환되는거 사용함. (기본스펙이 null 가능하게 설계되었음..) */
    val query: GsonData = input["queryStringParameters"]


    //==================================================== 헤더값 ======================================================

    val headers = input["headers"]

    /** IP */
    val ip: String = headers["x-forwarded-for"].str ?: ""

    /** UA */
    val ua: String = headers["user-agent"].str ?: ""

    companion object {
        fun extractPath(input: GsonData): String? = input["rawPath"].str ?: input["path"].str
    }
}