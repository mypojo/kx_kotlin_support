package net.kotlinx.core.number

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RangeSpliterTest : TestRoot() {

    @Test
    fun test() {

        val limit = 620000
        val step = 15L
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

        val headers = listOf("index") + List(spliters.size) { "spliter-${it}" } + listOf("7분할(${dayOfWeek}요일)", "sum")

        headers.toTextGrid(datas).print()


    }


}