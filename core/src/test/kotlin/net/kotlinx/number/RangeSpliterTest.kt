package net.kotlinx.number

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGrid
import java.time.LocalDate

class RangeSpliterTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("RangeSpliter") {

            val limit = 620000
            val step = 15L

            Then("데이터수 $limit / $step 개로 데이터세트 분리 -> 비교출력") {

                val spliters = listOf(
                    RangeSpliter {
                        minmax = 0L until limit
                        stepCnt = step
                    },
                    RangeSpliter {
                        minmax = 0L until limit
                        stepCnt = step
                        cycleCnt = 3
                    },
                )

                val dayOfWeek = LocalDate.now().dayOfWeek
                val datas = (0 until 15).map { i ->

                    val results = spliters.map { it[i] }

                    //분리형태 추가
                    val currentWeek = results[0].split(7, dayOfWeek.value)

                    val data = listOf(i) + results + listOf(currentWeek) + listOf(results.sumOf { it.size })
                    data.toTypedArray()
                }

                val headers = listOf("index", "${spliters[0].cycleCnt} 회 반복", "${spliters[1].cycleCnt} 회 반복") + listOf("7분할중 ${dayOfWeek}요일 처리건", "sum")

                headers.toTextGrid(datas).print()
            }
        }
    }

}