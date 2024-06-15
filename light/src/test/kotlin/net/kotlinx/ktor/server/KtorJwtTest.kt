package net.kotlinx.ktor.server

import io.kotest.matchers.shouldBe
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.kotest.modules.ktor.KtorMember
import net.kotlinx.kotest.modules.ktor.KtorMemberConverter

class KtorJwtTest : BeSpecHeavy() {

    private val memberConverter by koinLazy<KtorMemberConverter>()

    init {
        initTest(KotestUtil.FAST)

        Given("KtorMemberConverter") {

            val member = KtorMember("sin", "ADMIN")

            Then("JWT 변환 & 역변환") {

                val token = memberConverter.convertTo(member)

                val decoded = memberConverter.convertFrom(token)
                decoded shouldBe member
            }
        }
    }

}
