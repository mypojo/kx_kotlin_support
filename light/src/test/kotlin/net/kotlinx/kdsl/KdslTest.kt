package net.kotlinx.kdsl

import net.kotlinx.core.Kdsl
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class KdslTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("@DslMarker 테스트") {

            @Kdsl  
            class AnotherInnerDsl {
                constructor(block: AnotherInnerDsl.() -> Unit = {}) {
                    apply(block)
                }

                var anotherProperty: String = ""
            }

            @Kdsl
            class InnerDsl {

                constructor(block: InnerDsl.() -> Unit = {}) {
                    apply(block)
                }

                var innerProperty: String = ""

                fun anotherInnerDsl(block: AnotherInnerDsl.() -> Unit): AnotherInnerDsl {
                    return AnotherInnerDsl(block)
                }
            }

            @Kdsl
            class OuterDsl {
                constructor(block: OuterDsl.() -> Unit = {}) {
                    apply(block)
                }

                var outerProperty: String = ""

                fun innerDsl(block: InnerDsl.() -> Unit): InnerDsl {
                    return InnerDsl(block)
                }
            }

            Then("정상적인 DSL 사용 - 각 레벨에서만 해당 스코프 사용") {
                // 이 코드는 정상적으로 컴파일되어야 함
                OuterDsl {
                    outerProperty = "outer"

                    innerDsl {
                        innerProperty = "inner"

                        anotherInnerDsl {
                            anotherProperty = "another"
                        }
                    }
                }

                println("정상적인 DSL 사용 테스트 통과")
            }

            Then("DSL 마커로 인한 컴파일 에러 예시") {
                // 이제 아래 코드들이 실제로 컴파일 에러를 발생시킵니다
                
                OuterDsl {
                    outerProperty = "outer"


                    innerDsl {
                        innerProperty = "inner"

                        //innerDsl {} //  @Kdsl 이 달려있는경우 , 자기 자신에서 호출되는게 아니라면 컴파일 에러남


                        // 에러: 다른 레벨의 DSL 스코프에 접근 시도
                        // outerProperty = "이것은 컴파일 에러를 발생시킴"

                        anotherInnerDsl {
                            anotherProperty = "another"

                            // 에러: 더 상위 레벨의 DSL 스코프에 접근 시도
                            // innerProperty = "이것도 컴파일 에러를 발생시킴"
                            // outerProperty = "이것도 컴파일 에러를 발생시킴"
                        }
                    }
                }
                println("DSL 마커 컴파일 에러 방지 확인됨")
            }
        }
    }
}