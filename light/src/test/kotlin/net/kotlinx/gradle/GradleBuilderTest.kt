package net.kotlinx.gradle

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.io.File

class GradleBuilderTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("GradleBuilder") {

            val projectName = "xxx"
            val build = GradleBuilder("289023186990", projectName) {
                layers = listOf(
                    "${projectName}-layer_v1-${suff}",
                    "${projectName}-layer_v2-${suff}",
                    "${projectName}-layer_v3-${suff}",
                )
                println("빌드정보 ### $this")
            }

            xThen("??") {
                val jarFile = File("C:\\WORKSPACE\\11H11M\\${projectName}\\${projectName}_fun_ui\\build\\libs\\${projectName}_fun_ui-0.1.jar")
                build.updateLambda("${projectName}-fn_ui-${build.suff}", jarFile)
            }
        }
    }

}