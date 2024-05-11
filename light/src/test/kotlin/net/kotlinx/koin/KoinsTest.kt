package net.kotlinx.koin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.endWith
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koins
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.*
import org.koin.core.error.NoDefinitionFoundException

class KoinsTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("커스텀 주입 테스트") {

            Then("동일한 객체 2개 입력 -> 마지막으로 주입된게 로드됨") {
                val poo = koin<KoinData1>()
                poo.name shouldBe endWith("-1")
            }

            Then("bind 로 묶으면 리스트로 리턴 가능") {
                val koinDatas = koins<KoinData>()
                koinDatas.size shouldBe 2
            }

            When("인젝션") {
                Then("객체 인젝션 -> 주입이 되어있음") {
                    val service = koin<PooService>()
                    service.ex01.name shouldBe "특수관리"
                }

                Then("객체 생성 -> 그래도 주입이 되어있음 (런타임 주입)") {
                    val service = PooService()
                    service.ex01.name shouldBe "특수관리"
                    service.data2.name shouldBe "테스트2"
                }
            }

            When("named 주입") {

                Then("네이밍된 이름으로 타게팅해서 주입 가능") {
                    val koinData3 = koin<KoinData3>("ex03")
                    koinData3.name shouldBe "특수관리"
                    val koinData1 = koin<KoinData1>("ex01")
                    koinData1.name shouldBe "특수관리"
                }

                Then("named로만 주입된건 기본으로 찾지 못함 (스프링하고 틀림)") {
                    shouldThrow<NoDefinitionFoundException> {
                        koin<KoinData3>()
                    }
                }
            }

        }

    }

}
