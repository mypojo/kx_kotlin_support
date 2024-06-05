package net.kotlinx.kotest.modules.ktor

import mu.KotlinLogging
import net.kotlinx.koin.KoinModule
import net.kotlinx.ktor.server.KtorApplicationUtil
import net.kotlinx.ktor.server.KtorJwt
import org.koin.core.module.Module
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object KtorModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        /**
         * ktor 플러그인 설정
         * 람다 호출용 httpClient 리턴
         * */
        single {
            KtorApplicationUtil.buildClient {
                allModules()
            }
        }

        /** JWT 설정*/
        single {
            KtorJwt {
                secretKey = "kx_support"
                issuer = "kotlinx.net"
                audiences = listOf("user")
            }
        }
        single { KtorMemberConverter() }

    }

}