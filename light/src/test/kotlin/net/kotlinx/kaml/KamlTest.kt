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

            Then("읽기") {
                val code = "NHN"
                val root = File("C:\\Users\\${code}\\IdeaProjects\\dmp\\service\\api\\src\\main\\resources")
                val text = root.slash("api-docs.yaml").readText()
                val result = KamlDemoUtil.filterByTag(text, "bedrock")

                //println("=== org ")
                //println(text)

                println("=== reformat ")
                println(result)


                //root.slash("api-docs_bedrock.yaml").writeText(result)

                //aws49.bra.updateAgentActionGroupSchema("L8YEZD07X9", "IL5F1ALNYA", result)
            }

        }
    }

}