package net.kotlinx.kotest.modules.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import net.kotlinx.koin.Koins.koin
import net.kotlinx.ktor.server.KtorJwt

data class KtorUser(val name: String, val role: String) : Principal

fun Application.configureSecurity() {

    val log = KotlinLogging.logger {}

    authentication {

        /** 기본 인증 (name 없음)*/
        jwt {
            val jwt = koin<KtorJwt>()
            verifier { jwt.verifier }
            validate { credential ->
                //JWT는 용량이 중요해서, 리플렉션 돌리지 말고 직접 코딩할것
                check(credential.issuer == jwt.issuer)
                KtorUser(
                    name = credential.payload.getClaim("name").asString(),
                    role = credential.payload.getClaim("role").asString(),
                )
            }
        }

        /** 특수인증 ex) 벤더가 단순인증 요구 (name 지정) */
        bearer("bearer") {
            authenticate {
                log.warn { " bearer 임시 강제 로그인 $it" }
                UserIdPrincipal("ROLE_BEARER")
            }
        }

    }

    routing {

        get("/protected/a") {
            val principal = call.principal<UserIdPrincipal>()
            call.respondText("Hello ${principal?.name ?: "xx"}")
        }

        authenticate {
            get("/protected/b") {
                val principal = call.principal<KtorUser>()!!
                call.respondText("Hello ${principal.name} / ${principal.role}")
            }
        }

        authenticate("bearer") {
            get("/protected/c") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
    }
}
