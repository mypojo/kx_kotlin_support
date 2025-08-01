package net.kotlinx.google.calendar

import net.kotlinx.concurrent.delay
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

internal class GoogleCalendarTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("GoogleCalendar") {
            val calendar by koinLazy<GoogleCalendar>()
            val calId = "yyy@group.calendar.google.com"

            Then("리스팅") {
                val events = calendar.list(calId)
                events.take(4).forEach {
                    log.info { "${it.start.date} -> ${it.summary}" }
                }
            }

            xThen("이벤트 생성 / 수정 -> 쓰레기 데이터 생성되서 실행안함..삭제를 만들어야해") {
                val event = calendar.insert(calId) {
                    title = "긴급작업5"
                    desc = "사실 별거아님"
                    date = LocalDate.now() to LocalDate.now()
                }

                3.seconds.delay()
                event.title = "작업 종료!!"
                calendar.update(calId, event)
            }
        }
    }
}