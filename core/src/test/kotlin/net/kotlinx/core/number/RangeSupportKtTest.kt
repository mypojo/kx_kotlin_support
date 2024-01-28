package net.kotlinx.core.number

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class RangeSupportKtTest : TestRoot() {

    @Test
    fun test() {

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