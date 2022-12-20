package net.kotlinx.module1.csv

import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter
import kotlin.io.path.deleteIfExists
import kotlin.io.path.useLines

internal class CsvUtilTest {

    @Test
    fun 기본테스트() {

        val rows = listOf(listOf(1, 2, 3, "영감님"), listOf(4, 5, 6, "이동식2"))
        val file1 = File("D:\\DATA\\WORK/data1.csv")
        CsvUtil.ms949Writer().writeAll(rows, file1)

        CsvUtil.ms949Reader().readAll(file1).forEach {
            println(it)
        }

        val file2 = Paths.get("D:\\DATA\\WORK/data2.csv") //path

        //파일 배치로 읽을때
        file2.bufferedWriter().use {  out ->
            file1.useLines(
                charset("MS949")
            ) { lines ->
                lines.forEach {
                    println("기본 SDK $it")
                    out.write("기본 SDK $it\n")
                }
            }
        }

        file2.useLines{ lines ->
            lines.forEach {
                println("file2 다시읽기 $it")
            }
        }

        file1.delete()
        file2.deleteIfExists()


    }

}