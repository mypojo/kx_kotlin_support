package net.kotlinx.lazyLoad

import io.kotest.matchers.shouldBe
import net.kotlinx.core.Kdsl
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class LazyDefaultPropertyTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("LazyDefaultProperty") {

            When("기본값 테스트") {

                class DemoConfig {
                    @Kdsl
                    constructor(block: DemoConfig.() -> Unit = {}) {
                        apply(block)
                    }

                    lateinit var name: String
                    var age: Int = 0
                    var title: String by default { "나는 $age 세 $name 이다" }
                }

                Then("설정 안하면 기본값 출력"){
                    val demo1 = DemoConfig {
                        name = "영감님"
                        age = 100
                    }
                    demo1.title shouldBe "나는 100 세 영감님 이다"
                }

                Then("직접 설정하면 설정값 출력") {
                    val demo2 = DemoConfig {
                        name = "할망구"
                        age = 80
                        title = "꽃순이 17세"
                    }
                    demo2.title shouldBe "꽃순이 17세"
                }
                
                Then("값이 재설정되면 변경된 값이 출력됨") {
                    val demo3 = DemoConfig {
                        name = "청년"
                        age = 20
                    }
                    demo3.title shouldBe "나는 20 세 청년 이다"
                    
                    demo3.title = "새로운 제목"
                    demo3.title shouldBe "새로운 제목"
                }
                
                Then("속성 접근 시마다 기본값이 새로 생성되지 않음") {
                    val demo4 = DemoConfig {
                        name = "꼬마"
                        age = 5
                    }
                    val firstAccess = demo4.title
                    
                    // age 변경 후에도 title은 이미 초기화되었으므로 변경되지 않아야 함
                    demo4.age = 10
                    demo4.title shouldBe firstAccess
                    demo4.title shouldBe "나는 5 세 꼬마 이다" // 초기 접근 시 생성된 값이 유지됨
                }
            }
        }
    }
}

// 편의를 위한 확장 함수 (테스트 코드에서 사용 중인 것으로 보임)
fun <T> default(defaultFactory: () -> T) = LazyDefaultProperty(defaultFactory)