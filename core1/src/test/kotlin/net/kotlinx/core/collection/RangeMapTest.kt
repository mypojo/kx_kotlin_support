package net.kotlinx.core.collection

import org.junit.jupiter.api.Test

class RangeMapTest {

    @Test
    fun test() {

        val rangeMap = RangeMap(
            listOf(
                "20230701".."20230706" to "data2",
                "20230101".."20231201" to "data1",
            )
        )

        check(rangeMap["20230301"] == "data1")
        check(rangeMap["20230702"] == "data2")

        check(rangeMap["20220101"] == null)

        checkNotNull(rangeMap["20230101"])
        checkNotNull(rangeMap["20231201"])

    }

}