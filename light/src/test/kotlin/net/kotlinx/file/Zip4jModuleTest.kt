package net.kotlinx.file

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

class Zip4jModuleTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("Zip4jModule") {
            Then("비밀번호를 걸어서 압죽") {
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

}