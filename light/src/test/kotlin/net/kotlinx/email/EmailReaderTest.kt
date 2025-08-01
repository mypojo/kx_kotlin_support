package net.kotlinx.email

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

/**
 * EmailReader 테스트 클래스
 */
class EmailReaderTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("EmailReader") {

            val reader = EmailReader {
                username = "yy"
                password = "xx"
            }
            Then("이메일 리스팅") {
                reader.connect { folder ->
                    folder.list(2).print()
                }
            }

            Then("이메일 다운로드") {
                reader.connect { folder ->
//                    val datas = folder.list(2)
//                    val msg = datas.find { it.messageNumber == 1254 }!!

                    val files = folder.downloadAllFiles(1254)
                    println(files)

                }
            }


        }
    }
}