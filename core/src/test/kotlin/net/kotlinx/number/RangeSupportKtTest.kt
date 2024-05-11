package net.kotlinx.number

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGrid

class RangeSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("RangeSupportKt") {
            Then("구간 정의 -> 결과확인") {
                val datas = listOf(
                    0..100,
                    50..100,
                    0 until 100,
                    50 until 100,
                    0..0,
                    0 until 0,
                    -13..3,
                    13..3,
                ).map {
                    arrayOf(it, it.size)
                }

                listOf("데이터", "size").toTextGrid(datas).print()
            }
        }
    }

}