package net.kotlinx.koin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.koin.Koins.koins
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.*
import org.koin.core.context.loadKoinModules
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicLong

class KoinsTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("KoinData1") {

            Then("커스텀 네이밍 가져오기") {
                val koinData1 = koin<KoinData1>("ex01")
                koinData1.name shouldBe "테스트1-ex01"
            }

            Then("커스텀 제너릭 가져오기") {
                val KoinData5 = koin<KoinData5<KoinData2>>()
                KoinData5.name shouldBe "data02"
            }

            When("동일한 객체 2개 입력시") {
                Then("마지막으로 주입된게 로드됨 (커스템 제외)") {
                    val poo = koin<KoinData1>()
                    poo.name shouldBe "테스트1-오버라이드"
                }
            }

        }

        Given("KoinData2") {
            When("동일한 객체 2개 입력시") {
                Then("withOptions 은 루트도 입력함 -> 오버라이드됨") {
                    val root = koin<KoinData2>()
                    val ex = koin<KoinData2>("ex02")
                    root shouldBe ex
                }
            }
        }

        Given("KoinData3") {
            When("파라메터 주입해서 사용시") {
                Then("파라메터 주입시 정상동작") {
                    val parametered: KoinData3 = koin { parametersOf("xx") }
                    parametered.name shouldBe "xx"
                }
                Then("동일한거  파라메터 변경해도 첫번째 만든 캐시가 리턴됨") {
                    val parametered: KoinData3 = koin { parametersOf("yy") }
                    parametered.name shouldNotBe "yy"
                    parametered.name shouldBe "xx"
                }
            }
        }
        Given("KoinData4") {
            Then("named로만 주입된건 기본으로 찾지 못함 (스프링하고 틀림)") {
                shouldThrow<NoDefinitionFoundException> {
                    koin<KoinData4>()
                }
            }
        }

        Given("일반 내용") {
            Then("현황 출력") {
                Koins.printAll()
            }
            Then("bind 로 묶으면 리스트로 리턴 가능") {
                val koinDatas = koins<KoinData>()
                koinDatas.size shouldBeGreaterThan 1
            }

            Then("객체별로 네이밍 컨텍스트가 틀림 -> 동일이름도 구분가능") {
                val qualifier = "ex01"
                val ex01_1 = koin<DeveloperData>(qualifier)
                ex01_1.id shouldBe "kim"

                val koinData1 = koin<KoinData1>(qualifier)
                koinData1.name shouldBe "테스트1-ex01"
            }
        }

        Given("객체안의 인젝션") {
            When("PooService") {

                val ex01 = koin<KoinData1>("ex01")

                Then("객체 인젝션 -> 주입이 되어있음") {
                    val service = koin<PooService>()
                    service.ex01 shouldBe ex01
                }

                Then("객체 직접 생성 -> 그래도 주입이 되어있음 (런타임 주입)") {
                    val service = PooService()
                    service.ex01 shouldBe ex01

                    val data2: KoinData2 = koin()
                    service.data2 shouldBe data2
                }
            }
        }

        Given("런타임 모듈 설정") {

            Then("처음에는 없음") {
                shouldThrow<Exception> { koin<AtomicLong>() }
            }

            val magicNum = AtomicLong(100)
            Then("런타임 주입 가능") {
                loadKoinModules(module {
                    single { magicNum }
                })
                koin<AtomicLong>() shouldBe magicNum
            }

            Then("런타임 교체도 가능") {
                loadKoinModules(module {
                    single { AtomicLong(99) }
                })
                koin<AtomicLong>().get() shouldBe 99
            }

        }

        Given("지연로드 확인") {

            var cnt = 0

            class Poo1 {
                init {
                    cnt++
                    log.info { "객체 생성됨 -> $cnt" }
                }

                fun poo() {}
            }

            loadKoinModules(module {
                single { Poo1() }.withOptions { named("p1") }
                single { Poo1() }.withOptions { named("p2") }
            })

            When("일반은 즉시 로드") {
                printName()
                val poo = koin<Poo1>("p1")
                Then("객체 생성 -> 로직 시작") {
                    cnt shouldBe 1  //이미 생성됨
                    log.info { "p1 로직 시작.." }
                    poo.poo()
                    cnt shouldBe 1
                }
            }
            When("by는 늦은 로드 (실제 객체 접근시 생성)") {
                printName()
                val poo by koinLazy<Poo1>("p2")
                Then("로직 시작 -> 객체 생성") {
                    cnt shouldBe 1 //아직 생성 안됨
                    log.info { "p2 로직 시작.." }
                    poo.poo()
                    cnt shouldBe 2
                }
            }

        }

        Given("격리된 컨텍스트") {

            When("새로운 격리 컨텍스트 부여") {
                val newKoinApp = koinApplication {
                    // declare used modules
                    modules(module {
                        single { KoinData1("격리된 컨텍스트") } withOptions { this.named("ex01") }
                    })
                }
                Then("각 컨텍스트에서 별도의 앱 호출가능") {
                    val oldContext = koin<KoinData1>("ex01")
                    oldContext.name shouldBe "테스트1-ex01"

                    val newContext = newKoinApp.koin.get<KoinData1>(named("ex01"))
                    newContext.name shouldBe "격리된 컨텍스트"
                }
            }

        }


    }

}
