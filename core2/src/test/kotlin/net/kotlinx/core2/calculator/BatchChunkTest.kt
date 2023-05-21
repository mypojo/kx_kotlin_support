package net.kotlinx.core2.calculator

import io.kotest.matchers.shouldBe
import net.kotlinx.core2.gson.GsonData
import net.kotlinx.core2.gson.GsonSet
import org.junit.jupiter.api.Test

class BatchChunkTest {

    data class Qq(
        val name:String
    ){
        lateinit var aa:String

    }

    @Test
    fun poo() {

        //val qq = Qq("aa").apply { aa = "123" }
        val qq = Qq("aa")
        println(qq)

        val json = GsonSet.GSON.toJson(qq)
        println(json)

        val q2 = GsonData.parse(json).fromJson<Qq>()
        println(q2)
        println(q2.aa)



    }

    @Test
    fun getPageCnt() {

        BatchChunk(999, 500).also { it.maxPageNo shouldBe 2 }
        BatchChunk(1000, 500).also { it.maxPageNo shouldBe 2 }
        BatchChunk(1001, 500).also { it.maxPageNo shouldBe 3 }

        BatchChunk(105, 33).also {
            it.maxPageNo shouldBe 4
            it.range(1) shouldBe (1L to 33L)
            it.range(3) shouldBe (67L to 99L)
            it.range(4) shouldBe (100L to 105L)
        }

    }
}