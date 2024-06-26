package net.kotlinx.notion

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class NotionDatabaseToGoogleCalendarTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("NotionDatabaseToGoogleCalendar") {
            val synch = NotionDatabaseToGoogleCalendar {
                notionDbId = "48741c1766314c14938901047680703d"
                notionPageId = "4b18e3f52ce84487b64acab8ab2b5837"
                title = "이벤트명"
                desc = "내용상세"
                date = "날짜"
                type = "구분"
                calendarDefaultId = "b8291e41a4d3ddd3e8c91eca770464aa6f90b32a33e561b4e0c30b1fd22232b7@group.calendar.google.com"
                calendarTypeIdMap = mapOf(
                    "회사" to "va5ki7q0uqcg13re1re23l2frg@group.calendar.google.com",
                    "이벤트" to "cjmvo8554i4rmm2sq8utbokvbs@group.calendar.google.com",
                )
            }
            Then("노션을 구글캘린더와 연동") {
                synch.updateOrInsert()
            }
        }
    }

}