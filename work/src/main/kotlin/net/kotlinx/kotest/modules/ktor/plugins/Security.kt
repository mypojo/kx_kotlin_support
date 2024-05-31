package net.kotlinx.kotest.modules.ktor.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.modules.ktor.KtorMember
import net.kotlinx.ktor.server.KtorJwt
import net.kotlinx.ktor.server.forwardedIp
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.time.toKr01

fun Application.configureSecurity() {

    val log = KotlinLogging.logger {}

    /**
     * 기본적인 IP 체크 로직 추가 &
     * 중복 intercept 해도됨 = 소스코드 위치에 자유로움
     * */
    intercept(ApplicationCallPipeline.Setup) {
        // 이곳에서 공통 체크 로직을 구현합니다.
        log.trace { "IP 체크.." }
        val ip = call.request.forwardedIp
        if (ip == "111.222.333.444") {
            call.respondText("Invalid Ip Address $ip", status = HttpStatusCode.Forbidden)
            finish() // 다음 처리 단계로 이동하지 않고 중단
        }
    }

    authentication {

        /** 기본 인증 (name 없음)*/
        jwt {
            val jwt = koin<KtorJwt>()
            verifier { jwt.verifier }
            validate { credential ->
                //JWT는 용량이 중요해서, 리플렉션 돌리지 말고 직접 코딩할것
                check(credential.issuer == jwt.issuer)
                log.debug { " 제시된 credential => ${credential.payload.claims} / expire : ${credential.expiresAt!!.time.toLocalDateTime().toKr01()}" }
                KtorMember(
                    name = credential.payload.getClaim("name").asString(),
                    roleGroup = credential.payload.getClaim("roleGroup").asString(),
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
                val principal = call.principal<KtorMember>()!!
                call.respondText("Hello ${principal.name} / ${principal.roleGroup}")
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
