package net.kotlinx.module.xlsx

import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.threadlocal.ResourceHolder
import org.junit.jupiter.api.Test
import java.io.File

class Excel_읽기 : TestRoot() {


    @Test
    fun test() {

        val file = File(ResourceHolder.getWorkspace(), "excel/demo.xlsx").apply { parentFile.mkdir() }
        val xls = Excel.from(file)

        xls.readAll().entries.forEach {
            println(it)
        }

    }

}