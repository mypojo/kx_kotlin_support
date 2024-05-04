package net.kotlinx.excel

import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test
import java.io.File

class Excel_읽기 : BeSpecLog(){
    init {
        @Test
        fun test() {

            val file = File(ResourceHolder.getWorkspace(), "excel/demo.xlsx").apply { parentFile.mkdir() }
            val xls = Excel.from(file)

            xls.readAll().entries.forEach {
                println(it)
            }

        }
    }
}