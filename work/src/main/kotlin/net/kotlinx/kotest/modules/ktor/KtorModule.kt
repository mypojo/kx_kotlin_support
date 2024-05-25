package net.kotlinx.kotest.modules.ktor

import mu.KotlinLogging
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.ktor.server.KtorApplicationUtil
import net.kotlinx.ktor.server.KtorJwt
import org.koin.core.module.Module
import org.koin.dsl.module

data class KtorMember(val name: String, val roleGroup: String)

interface DataConverter<T> {

    fun encode(data: T): String

    fun decode(token: String): T

}

class KtorMemberConverter : DataConverter<KtorMember> {

    private val jwt by koinLazy<KtorJwt>()

    override fun encode(data: KtorMember): String = jwt.createToken(
        mapOf(
            KtorMember::name.name to data.name,
            KtorMember::roleGroup.name to data.roleGroup,
        )
    )

    override fun decode(token: String): KtorMember {
        val claimMap = jwt.parseToken(token)
        return KtorMember(
            name = claimMap[KtorMember::name.name]!!.asString(),
            roleGroup = claimMap[KtorMember::roleGroup.name]!!.asString(),
        )
    }


}

/** 해당 패키지의 기본적인 의존성 주입 */
object KtorModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        /** ktor 람다 호출용 httpClient */
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