package net.kotlinx.module1.csv

import org.junit.jupiter.api.Test
import java.io.File

internal class CsvUtilTest{

    @Test
    fun `기본테스트`(){

        val rows = listOf(listOf(1, 2, 3, "영감님"), listOf(4, 5, 6,"이동식2"))
        val file = File("D:\\DATA\\WORK/data2.csv")
        CsvUtil.ms949Writer().writeAll(rows,file)

        CsvUtil.ms949Reader().readAll(file).forEach {
            println(it)
        }


    }

}