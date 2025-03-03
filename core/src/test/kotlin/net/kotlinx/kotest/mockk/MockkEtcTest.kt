package net.kotlinx.kotest.mockk

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.kotlinx.collection.MapSupport
import net.kotlinx.collection.toQueryString
import net.kotlinx.core.PackageNameSupport
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.StringNumberSupport
import net.kotlinx.string.toBigDecimal2
import net.kotlinx.system.DeploymentType
import net.kotlinx.time.LocalDatetimeSupport
import java.math.BigDecimal
import java.time.LocalDateTime

object MockTest : PackageNameSupport

/**
 *  Mockk 테스트 샘플
 */
class MockkEtcTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("기본 모킹 테스트") {

            When("java static (외부라이브러리) 모킹") {

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


            When("확장함수 모킹") {

                Then("정상 모킹됨") {
                    log.trace { "컴파일되면 이름 변경됨 StringNumberSupport.kt -> StringNumberSupportKt" }
                    mockkStatic(StringNumberSupport.packageName())

                    every { "333".toBigDecimal2() } returns BigDecimal("777")

                    "333".toBigDecimal2() shouldBe 777.toBigDecimal() //모킹됨
                    "444".toBigDecimal2() shouldBe 444.toBigDecimal() //모킹되지 않음
                }

                Then("모킹 풀면 돌아감") {
                    unmockkStatic(StringNumberSupport.packageName())
                    "333".toBigDecimal2() shouldBe 333.toBigDecimal() //모킹됨
                }

                When("모킹 대상은 객체임") {
                    mockkStatic(MapSupport.packageName())

                    val 모킹적용 = mapOf(
                        "a" to "b"
                    )
                    val 모킹적용x = mapOf(
                        "a" to "x"
                    )

                    Then("모킹 적용건만 변경됨") {
                        모킹적용.toQueryString() shouldBe "a=b"
                        every { 모킹적용.toQueryString() } returns "몰라요"

                        모킹적용.toQueryString() shouldBe "몰라요"
                        모킹적용x.toQueryString() shouldBe "a=x"
                    }

                    unmockkStatic(MapSupport.packageName())

                    Then("모킹 풀면 동일해짐") {
                        모킹적용.toQueryString() shouldBe "a=b"
                    }
                }

                When("inline 함수 모킹") {

                    mockkStatic(MockkTestSupport.packageName())

                    Then("인라인 모킹하면 에러남") {
                        shouldThrow<MockKException> {
                            every { "xx".toMockkInlineString1() } returns "몰라요1"
                        }
                    }

                    Then("인라인 아닌거는 정상작동함") {
                        every { "xx".toMockkInlineString2() } returns "몰라요2"
                        "xx".toMockkInlineString2() shouldBe "몰라요2"
                        "aa".toMockkInlineString2() shouldBe "인라인2"
                    }

                    unmockkStatic(LocalDatetimeSupport.packageName())
                }

            }

        }


    }

}