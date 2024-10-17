package net.kotlinx.spring.thread

import net.kotlinx.concurrent.sleep
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.toKr01
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.days

class ThreadPoolBuilderTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("ThreadPoolBuilder 테스트") {
            Then("로컬 스케쥴링") {

                val scheduler = ThreadPoolBuilder(10).build {

                }

                scheduler.scheduleAtFixedDate(13,46){
                    log.info { "스케쥴 실행됨!!  현재시간 ${LocalDateTime.now().toKr01()}" }
                }

                log.info { "스래드 대기중..." }
                1.days.sleep()
                scheduler.shutdown()
            }
        }
    }

}
