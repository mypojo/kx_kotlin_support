package net.kotlinx.email

import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import net.kotlinx.collection.toPair
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.lazyLoad.lazyLoadStringSsm

/**
 * EmailReader 테스트 템플릿
 * - 실제 동작 코드는 없이 파일과 기본 골격만 제공합니다.
 */
class EmailReaderTest : BeSpecLight() {

    init {
        // 템플릿만 제공하므로 테스트는 실행하지 않도록 설정
        initTest(KotestUtil.IGNORE)

        Given("EmailReader 동작 테스트") {

            val email = "aa@bb.com"
            val pair = email.split("@").toPair()
            val pwd by lazyLoadStringSsm("/secret/api/email/${pair.first}/${pair.second}","xx")
            val reader = EmailReader {
                host = "imap.dooray.com"
                username = email
                password = pwd
            }
            Then("이메일 리스팅") {
                val days = 10
                reader.connect {
                    val emailDatas = listEmailDatas(days).take(4)
                    emailDatas.printSimple()
                    //emailDatas.size shouldBeGreaterThanOrEqual 2
                }
            }

            Then("이메일 다운로드 -> 오래걸림") {
                reader.connect {
                    val files = downloadAllFiles(1)
                    files.forEach {
                        log.info { " -> ${it.absolutePath}" }
                    }
                    files.size shouldBeGreaterThanOrEqual 1
                }
            }

        }
    }
}
