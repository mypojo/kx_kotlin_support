package net.kotlinx.aws.lambda

import net.kotlinx.okhttp.toQueryMap
import okhttp3.HttpUrl

/**
 * 람다 입력을 테스트용 코드
 * 각종 설정을 넣으면 API Gateway 형식의 json 설정 map으로 바꿔줌
 * json 이 아니고 map 인 이유는 람다 핸들러가 map으로 받기 때문.. ㅠㅠ
 *
 * 전형적인 위임의 활용 샘플
 * 샘플 참고 : https://www.notion.so/mypojo/Lamnda-Input-55a07a2c8a6d49eb92a1c90126e7f4be
 * */
class LambdaUrlMap(private val delegate: MutableMap<String, Any> = mutableMapOf(), block: LambdaUrlMap.() -> Unit = {}) : Map<String, Any> by delegate {

    //==================================================== 설정값 ======================================================

    var headers = mapOf(
        "x-forwarded-for" to "11.22.33.44",
        "user-agent" to "demo2",
    )

    lateinit var url: HttpUrl

    init {
        block()
        delegate["rawPath"] = url.encodedPath
        delegate["headers"] = headers
        delegate["queryStringParameters"] = url.toQueryMap()
    }

    override fun toString(): String {
        return delegate.toString()
    }

}