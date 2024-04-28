package net.kotlinx.core.collection

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.kotlinx.core.test.KotestUtil
import net.kotlinx.core.test.init

class MapTreeTest : BehaviorSpec({

    init(KotestUtil.FAST)

    Given("MapTree") {

        Then("생성자 방법") {
            val mapTree1 = MapTree {
                "안녕하세요 $it"
            }
            mapTree1["aa"] shouldBe "안녕하세요 aa"
        }

        When("atomicLong") {
            Then("맵으로 카운트 하는 기본 사용") {
                val mapTree = MapTree.atomicLong()
                (0..20).forEach {
                    mapTree["aa"].incrementAndGet()
                    mapTree["aa${it}"].incrementAndGet()
                }

                mapTree["aa19"].incrementAndGet()
                mapTree["aa19"].get() shouldBe 2
            }
        }

    }
})