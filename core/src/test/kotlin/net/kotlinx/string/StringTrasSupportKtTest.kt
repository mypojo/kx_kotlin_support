package net.kotlinx.string

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class StringTrasSupportKtTest : BehaviorSpec() {
    init {
        initTest(KotestUtil.FAST)

        Given("StringTrasSupportKt") {
            Then("간단체크") {
                "12345".isNumeric() shouldBe true
                "123.45".isNumeric() shouldBe false
            }
        }

        Given("쿼리 변환") {

            Then("간단변환") {
                val query = """
    select *                    
    from v3
    where basic_date = date_format(current_date, '%Y%m%d')
      and query = :query
      and media_adv_id = :media_adv_id
"""
                val replacedQuery = query.replaceSqlAll(
                    mapOf(
                        ":query" to "청바지",
                        ":media_adv_id" to 8677,
                    )
                )
                replacedQuery shouldBe contain("query = '청바지'")
                replacedQuery shouldBe contain("media_adv_id = 8677")
            }

        }
    }
}
