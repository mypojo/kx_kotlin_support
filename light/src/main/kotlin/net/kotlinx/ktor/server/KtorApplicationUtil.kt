package net.kotlinx.ktor.server

import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.testing.*

object KtorApplicationUtil {

    /**
     * 람다에서 사용하기위한 클라이언트
     * application 접근은 못함
     * test 의존성이 있어야 한다.
     *
     * 주의! 이 클라이언트는 멀티플랫폼용 ktor client가 아니고 테스트용 client이다.
     * 단지 람다에서 우회 사용하기 위해 이렇게 작업했을뿐임!
     * 더 좋은방법이 있었으면 좋겠다.
     *  */
    fun buildClient(block: Application.() -> Unit): HttpClient {
        val builder = ApplicationTestBuilder()
        builder.application(block)
        return builder.client
    }

}