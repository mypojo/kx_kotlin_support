package net.kotlinx.kaml

import net.kotlinx.file.slash
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.io.File


class KamlTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        //java.lang.NoClassDefFoundError: io/ktor/client/plugins/UserAgent
        //수정필요!!
        Given("데이터베이스") {

            Then("filterByTag") {
                val code = "nnn"
                val root = File("C:\\Users\\${code}\\IdeaProjects\\dmp\\service\\api\\src\\main\\resources")
                val text = root.slash("api-docs.yaml").readText()
                val result = KamlDemoUtil.filterByTag(text, "bedrock")
                println("=== reformat ")
                println(result)
            }

            Then("filterByAttribute") {
                val code = "nnn"
                val root = File("C:\\Users\\${code}\\IdeaProjects\\dmp\\service\\api\\src\\main\\resources")
                val text = root.slash("api-docs.yaml").readText()
                val result = KamlDemoUtil.filterByAttribute(text, "usage", "service", null)
                println("=== reformat ")
                println(result)
            }

        }
    }

}