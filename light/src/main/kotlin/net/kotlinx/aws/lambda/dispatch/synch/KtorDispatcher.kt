package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.collection.toQueryString
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * 람다 펑션 or API Gateway 처리기
 * 일반적으로 ktor 붙여서 사용
 *
 * https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html
 *  */
class KtorDispatcher : LambdaDispatch {

    private val httpClient by koinLazy<HttpClient>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {

        val rawPath = input["rawPath"].str ?: return null

        val queryString = input["queryStringParameters"].toMap()
        val headers = input["headers"].toMap()
        val response = httpClient.get("${rawPath}?${queryString.toQueryString()}") {
            headers {
                headers.forEach {
                    append(it.key, it.value ?: "") //이렇게 해도 되나? 필터링 해도 될듯
                }
            }
        }
        val body = response.bodyAsText()
        return KtorWebOutput(body)
    }


}

