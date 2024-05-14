package net.kotlinx.kotest.mockk

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest


/**
 *  Mockk 테스트 샘플
 */
class MockkMockkTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("mockk 는 전부 모킹한다") {

            When("DemoRepository 에 의존적인 테스트를 하고싶은데 DB 연결을 할 수 없음") {

                lateinit var demoRepository: DemoRepository
                lateinit var demoService: DemoService

                beforeTest {
                    demoRepository = kockDemoRepository()
                    demoService = DemoService(demoRepository)
                }


                Then("updateUser 로직을 거치면 나이가 한살 늘어나야함") {
                    val user = demoRepository.findById("123")
                    val updateUser = demoService.updateUser("123")
                    updateUser.age shouldBe user.age
                }
            }

        }
    }

}