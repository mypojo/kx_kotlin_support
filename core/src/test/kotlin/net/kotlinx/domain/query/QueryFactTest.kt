package net.kotlinx.domain.query

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class QueryFactTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("QueryModule") {

            val dimensions = listOf(
                QueryDimension {
                    name = "basic_date"
                    tables = listOf("rpt_a", "rpt_b")
                    desc = "날짜"
                },
                QueryDimension {
                    name = "member_id"
                    tables = listOf("rpt_a", "rpt_b")
                    desc = "회원 ID"
                },
                QueryDimension {
                    name = "member_name"
                    tables = listOf("rpt_a", "rpt_b")
                    desc = "회원 이름"
                },
            )

            val metrics = listOf(
                QueryFact {
                    name = "imp_cnt"
                    tables = listOf("rpt_a", "rpt_b")
                    desc = "노출 수"
                },
                QueryFact {
                    name = "click_sum"
                    tables = listOf("rpt_a", "rpt_b")
                    desc = "노출 수"
                },
                QueryFact {
                    name = "ca_sum"
                    tables = listOf("rpt_a", "rpt_c")
                    desc = "매출합계"
                },
            )

            val module = QueryModule("RPT", dimensions + metrics)

            Then("사용자의 입력을 받아서 쿼리를 완성해줌") {
                val input = listOf("basic_date", "member_id", "imp_cnt")
                val sql = module.build(input) {
                    "basic_date = '2014' "
                }
                println(sql)
            }
        }
    }

}

