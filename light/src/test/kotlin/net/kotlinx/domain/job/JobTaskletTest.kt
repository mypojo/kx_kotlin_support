package net.kotlinx.domain.job

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class JobTaskletTest : BeSpecLight() {

    /** onProcessComplete 를 오버라이드한 데모 클래스 */
    class OverrideJob : JobTasklet {
        override suspend fun execute(job: Job) {}
        override suspend fun onProcessComplete(job: Job) {
            // onProcessComplete 오버라이드됨
        }
    }

    /** onProcessComplete 를 오버라이드하지 않은 데모 클래스 */
    class NotOverrideJob : JobTasklet {
        override suspend fun execute(job: Job) {}
    }

    init {

        initTest(KotestUtil.FAST)

        Given("JobTasklet의 onProcessCompleteOverridden 테스트") {

            When("onProcessComplete 를 오버라이드 한 경우") {
                val job = OverrideJob()
                Then("onProcessCompleteOverridden() 은 true 여야 한다") {
                    job.onProcessCompleteOverridden() shouldBe true
                }
            }

            When("onProcessComplete 를 오버라이드 하지 않은 경우") {
                val job = NotOverrideJob()
                Then("onProcessCompleteOverridden() 은 false 여야 한다") {
                    job.onProcessCompleteOverridden() shouldBe false
                }
            }
        }
    }
}
