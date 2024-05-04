package net.kotlinx.file

import net.kotlinx.core.file.slash
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test

class Zip4jModuleTest : BeSpecLog() {
    init {
        @Test
        fun test() {


            val workspace = ResourceHolder.getWorkspace()

            Zip4jModule {
                files = listOf(
                    workspace.slash("temp-estimate_validate"),
                    //workspace.slash("230925_두쓰멍 영상소재 비율 수정(롱).mp4"),
                )
                password = "1234"
                targetZipFile = workspace.slash("result.zip")
                //zip()
                unzip()

            }

        }
    }
}