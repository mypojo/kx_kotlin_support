package net.kotlinx.test.jwt


import net.kotlinx.koin.KoinModule
import net.kotlinx.spring.security.jwt.JwtProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours


object MySecurityModule : KoinModule {

    override fun moduleConfig(option: String?): Module = module {

        single {
            //val secret = MyAws1.AWS.ssmStore["/web/token/secret/${MyEnv.SUFFIX}"]!!
            JwtProvider("시크릿키", 24.hours)
        }

    }

}



