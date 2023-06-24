package net.kotlinx.core.collection

import org.junit.jupiter.api.Test

class MapTreeTest {

    @Test
    fun test() {

        val mapTree = MapTree.atomicLong()
        (0..20).forEach {
            mapTree["aa"].incrementAndGet()
            mapTree["aa${it}"].incrementAndGet()
        }

        println(mapTree.delegate)


        val mapTree1 = MapTree {
            "aa $it"
        }

        println(mapTree1["aa"])
        println(mapTree1["bb"])


    }

}