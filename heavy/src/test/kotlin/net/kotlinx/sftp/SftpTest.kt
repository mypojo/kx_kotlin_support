package net.kotlinx.sftp

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class SftpTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("Sftp") {
            val config: SftpConfig = SftpConfig("web01", "localhost", "id", "pass")
            Then("파일 리스팅") {
                Sftp(config).use { sftp ->
                    val listFiles = sftp.ls("/home/xxx/logs/tomcat", false)
                    for (listFile in listFiles) {
                        println(listFile)
                    }
                }
            }
        }
    }


}