package net.kotlinx.spring.data

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import org.springframework.data.domain.PageRequest

class ListPageSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("page") {

            val list = (0..9).map { it }

            Then("지정된 페이지 리턴") {
                val page = list.page(PageRequest.of(1, 3))
                page.content shouldBe listOf(3, 4, 5)
            }

            Then("마지막 페이지 리턴 (남은거만 리턴됨)") {
                val page = list.page(PageRequest.of(3, 3))
                page.content shouldBe listOf(9)
            }

            Then("오버된 페이지는 빈값 리턴") {
                val page = list.page(PageRequest.of(4, 3))
                page.content shouldBe emptyList()
            }
        }

        Given("athena 쿼리 페이징 테스트") {
            val req = PageRequest.of(0, 4)
            req.pageingAthena shouldBe "OFFSET 0 ROWS LIMIT 4"
        }
    }

}
