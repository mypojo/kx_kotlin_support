package net.kotlinx.aws.lambdaUi

/**
 * 람다 URL 호출시 필요한 정보를 추출함
 * 참고로 context 에는 awsRequestId, logStreamName 등이 입력됨 -> 안씀
 *  */
class LambdaUrlInput(
    val input: Map<String, Any>
) {

    /** 경로. ex) /aa/bb */
    val path: String = input["rawPath"] as String

    /** 자동 변환되는거 사용함. (기본스펙이 null 가능하게 설계되었음..) */
    val query: Map<String, Any> = input["queryStringParameters"] as Map<String, Any>? ?: emptyMap()

    private val headers = input["headers"] as Map<String, Any>

    /** IP */
    val ip: String = headers["x-forwarded-for"] as String

    /** UA */
    val ua: String = headers["user-agent"] as String
}