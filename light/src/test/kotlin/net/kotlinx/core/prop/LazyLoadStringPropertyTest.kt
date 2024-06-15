package net.kotlinx.core.prop

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm

class LazyLoadStringPropertyTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("LazyLoadStringProperty") {
            Then("객체 설정으로 SSM에서 데이터를 가져오는 늦은 초기화") {

                class MyConfig {
                    private val log = KotlinLogging.logger {}
                    lateinit var name: String
                    var demo: String by lazyLoadStringSsm("/slack/token")
                }

                val myConfig = MyConfig()
                repeat(1000) {
                    log.trace { "여러번 호출해도 한번만 가져옴" }
                    myConfig.demo shouldNotBe null
                }
            }

            Then("SSM에서 불러온 데이터 리셋") {
                var demo: String by lazyLoadStringSsm("/slack/token")
                demo.length shouldBeGreaterThan 0

                log.info { "변경전 : $demo" }
                demo = "토큰을노출하면 안되요!"  //일반 문자열로 리셋
                log.info { "변경후 : $demo" }

                demo shouldBe "토큰을노출하면 안되요!"
            }

        }
    }

}