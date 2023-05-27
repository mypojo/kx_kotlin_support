package net.kotlinx.module.sftp

import org.junit.jupiter.api.Test

class SftpTest {

    val config: SftpConfig = SftpConfig("web01", "localhost", "id", "pass")

    @Test
    fun test() {

        Sftp(config).use { sftp ->
            val listFiles = sftp.ls("/home/xxx/logs/tomcat", false)
            for (listFile in listFiles) {
                println(listFile)
            }
        }
    }


}