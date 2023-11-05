package net.kotlinx.gradle

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.io.File

class GradleBuilderTest : TestRoot(){

    val projectName = "xxx"
    val build = GradleBuilder("289023186990", projectName) {
        layers = listOf(
            "${projectName}-layer_v1-${suff}",
            "${projectName}-layer_v2-${suff}",
            "${projectName}-layer_v3-${suff}",
        )
        println("빌드정보 ### $this")
    }

    @Test
    fun test() {

        val jarFile = File("C:\\WORKSPACE\\11H11M\\${projectName}\\${projectName}_fun_ui\\build\\libs\\${projectName}_fun_ui-0.1.jar")
        build.updateLambda("${projectName}-fn_ui-${build.suff}", jarFile)

    }

}