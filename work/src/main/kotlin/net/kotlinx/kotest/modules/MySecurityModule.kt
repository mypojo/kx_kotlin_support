package net.kotlinx.kotest.modules


import net.kotlinx.koin.KoinModule
import net.kotlinx.spring.security.jwt.JwtProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours


object MySecurityModule : KoinModule {

    override fun moduleConfig(): Module = module {


        /** 스프링 부트에서 사용하는 JWT 제공자.. 별로인거 같다. */
        single {
            JwtProvider("시크릿키", 24.hours)
        }

    }

}



