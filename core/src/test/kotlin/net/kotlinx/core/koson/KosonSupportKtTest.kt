package net.kotlinx.core.koson

import com.lectra.koson.obj
import net.kotlinx.core.csv.readCsvLines
import net.kotlinx.core.csv.writeCsvLines
import net.kotlinx.core.file.slash
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.toGsonData
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class KosonSupportKtTest : TestRoot(){


    @Test
    fun test() {


        val obj = obj {
            "text" to "\"테스트값"
        }

        println(obj.toGsonData()["text"].str)


        val csvLines = listOf(
            listOf(
                "title",
                obj.toString()
            )
        )


        val file = ResourceHolder.getWorkspace().slash("temp").slash("temp.csv")
        file. writeCsvLines(csvLines)

        val readLines = file.readCsvLines()
        readLines.forEach {
            println(it)
            println(GsonData.parse(it[1]))
            println(GsonData.parse(it[1])["text"].str)
        }


    }

    @Test
    fun test22() {
        val file = ResourceHolder.getWorkspace().slash("temp").slash("00001.txt.csv")
        val readLines = file.readCsvLines()
        println(readLines.size)

        readLines.forEach { line ->
            line[5].toGsonData()["NV_CRW"].forEach {
                println(it)
            }
        }

//        readLines.take(10).forEach {
//            println(it)
//        }
    }


}