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
                    )
                    password = "1234"
                    targetZipFile = workspace.slash("result.zip")
                    //zip()
                    unzip()

                }
            }

            Then("분할압축") {
                val workspace = ResourceHolder.getWorkspace().slash("업체제공데이터_월간")
                Zip4jModule {
                    files = listOf(
                        workspace.slash("제공데이터_202402.zip"),
                    )
                    targetZipFile = workspace.slash("result.zip")
                    splitSizeMb =  5 * 1024
                    zip()
                }
            }
        }
    }

}