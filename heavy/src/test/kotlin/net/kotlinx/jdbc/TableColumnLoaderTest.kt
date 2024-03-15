package net.kotlinx.jdbc

import net.kotlinx.core.file.listAllFiles
import net.kotlinx.core.file.slash
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.test.TestHeavy
import org.junit.jupiter.api.Test
import java.io.File

class TableColumnLoaderTest : TestHeavy() {

    @Test
    fun `텍스트파싱기`() {

        val root = File("C:\\WORKSPACE\\xx\\common\\src\\main\\java")

        val allFiles = root.listAllFiles().filter { it.name.endsWith(".java") }.mapNotNull { f ->

            val lines = f.readText().split("\n")
            val isEnum = lines.any { it.startsWith("public enum ") }
            if (isEnum) {
                log.debug { "${f.name} -> isEnum" }
                println(lines.size)
                println(f.readText())
            }

            if (isEnum) lines else null
        }

        println(allFiles[0])


    }


    @Test
    fun `DB딕셔너리`() {

        val setup = DataSourceSetupDemo_sin.load()
        val dataSource = setup.createDataSource {
            minimumIdle = 1
        }

        val loader = TableColumnLoader(dataSource, "${setup.profile}_dev")

        val columns = loader.loadMysqlColumns()
        val jsonText = GsonData.fromObj(columns)
        ResourceHolder.getWorkspace().slash("test").slash("tables.txt").writeText(jsonText.toString())

    }

}