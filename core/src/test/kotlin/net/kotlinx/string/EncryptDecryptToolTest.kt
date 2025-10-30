package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class EncryptDecryptToolTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("EncryptDecryptTool") {
            Then("암/목호화") {

                val kryword = "nhn"

                run {
                    val key = kryword
                    val tool = EncryptDecryptTool(key)
                    val input = "우리동네영감님 ^^; K8"
                    val en = tool.encrypt(input)
                    val result = tool.decrypt(en)
                    log.info { " key : ${key} ==> $en" }
                    input shouldBe result
                }

                run {
                    val key = kryword + "salt"
                    val tool = EncryptDecryptTool(key)
                    val input = "우리동네영감님 ^^; K8"
                    val en = tool.encrypt(input)
                    val result = tool.decrypt(en)
                    log.info { " key : ${key} ==> $en" }
                    input shouldBe result
                }

            }
        }
    }
}