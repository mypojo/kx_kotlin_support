package net.kotlinx.aws.lambdaFunction

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

/**
 * VPC 내부에서만 열려있는 http 요청을 프록싱 해주는 도구
 */
class HttpProxyHandler : RequestHandler<Map<String?, Any?>?, String> {

    override fun handleRequest(event: Map<String?, Any?>?, context: Context): String {
        throw UnsupportedOperationException("미구현")
    }
}