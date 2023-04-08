package net.kotlinx.core1.collection

import org.junit.jupiter.api.Test

class MapTreeTest {

    @Test
    fun test() {

        val mapTree = MapTree.atomicLong()
        (0..20).forEach {
            mapTree.get("aa").incrementAndGet()
            mapTree.get("aa${it}").incrementAndGet()
        }

        println(mapTree.delegate)


    }

}