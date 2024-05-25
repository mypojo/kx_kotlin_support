package net.kotlinx.ktor.server

import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.kotest.modules.ktor.KtorMember
import net.kotlinx.kotest.modules.ktor.KtorMemberConverter

class KtorJwtTest : BeSpecLight(){

    private val memberConverter by koinLazy<KtorMemberConverter>()

    init {
        initTest(KotestUtil.FAST)

        Given("KtorMemberConverter") {

            val member = KtorMember("sin", "ADMIN")

            Then("JWT 변환 & 역변환") {

                val token = memberConverter.encode(member)
                println(token)

                val decoded = memberConverter.decode(token)
                println(decoded)
            }
        }
    }

}
