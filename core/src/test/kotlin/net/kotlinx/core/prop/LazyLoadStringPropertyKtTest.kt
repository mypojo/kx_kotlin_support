package net.kotlinx.core.prop

import io.kotest.matchers.shouldNotBe
import net.kotlinx.core.Poo
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class LazyLoadStringPropertyKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("LazyLoadStringPropertyKt") {

            var creatCnt = 0
            val demo: Poo by lazyLoad {
                log.info { "초기화됩니다!! -> ${creatCnt++}" }
                Poo("매우 오래걸리는 작업", "demo", creatCnt)
            }

            Then("최초 할당 후 리셋됨 -> 초기화 결과값이 달라짐") {

                val demo01 = demo
                log.debug { "데이터로드1 $demo" }
                log.debug { "데이터로드2 $demo" }
                lzayLoadReset(Poo::class.java)
                log.debug { "데이터로드3 $demo" }
                val demo02 = demo

                demo01.age shouldNotBe demo02.age
            }
        }
    }

}