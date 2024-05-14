package net.kotlinx.kotest.mockk

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toBigDecimal2
import net.kotlinx.system.DeploymentType
import java.time.LocalDateTime

class MyClass {
    companion object {
        fun companionMethod(): String = "original"
    }
}


/**
 *  Mockk 테스트 샘플
 */
class MockkEtcTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("기본 모킹 테스트") {

            When("java static 모킹 필요") {

                Then("LocalDateTime.now() 호출시 모킹 날짜 리턴") {
                    mockkStatic(LocalDateTime::class) //클래스로 모킹 정의
                    every { LocalDateTime.now() } returns LocalDateTime.of(2023, 5, 3, 0, 0, 0)
                    LocalDateTime.of(2023, 5, 3, 0, 0, 0) shouldBe LocalDateTime.now()
                }

            }

            When("kotlin Companion 모킹 필요") {

                Then("Companion 객체를 모킹해서 결과 리턴") {
                    mockkObject(DeploymentType.Companion)  //static 이 아니라 객체이다!!
                    every { DeploymentType.load() } returns DeploymentType.PROD
                    DeploymentType.load() shouldBe DeploymentType.PROD
                }
            }


            Then("확장함수 모킹") {

                log.trace { "컴파일되면 이름 변경됨 StringNumberSupport.kt -> StringNumberSupportKt" }
                mockkStatic("net.kotlinx.string.StringNumberSupportKt")

                every { "333".toBigDecimal2() } returns 777.toBigDecimal()

                "333".toBigDecimal2() shouldBe 777.toBigDecimal() //모킹됨
                "444".toBigDecimal2() shouldBe 444.toBigDecimal() //모킹되지 않음

            }
        }
    }

}