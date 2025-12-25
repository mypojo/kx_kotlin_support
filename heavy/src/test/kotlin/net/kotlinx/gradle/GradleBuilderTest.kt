package net.kotlinx.gradle

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.io.File

class GradleBuilderTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("GradleBuilder") {

            val projectName = "xxx"
            val build = GradleBuilder {
                lambdaLayers = listOf(
                    "${projectName}-layer_v1-$suff",
                    "${projectName}-layer_v2-$suff",
                    "${projectName}-layer_v3-$suff",
                )
                println("빌드정보 ### $this")
            }

            xThen("람다 함수 배포") {
                val jarFile = File("C:\\WORKSPACE\\xxx\\${projectName}\\${projectName}_fun_ui\\build\\libs\\${projectName}_fun_ui-0.1.jar")
                build.lambdaUpdateFunction("${projectName}-fn_ui-${build.suff}", jarFile)
            }
        }
    }

}