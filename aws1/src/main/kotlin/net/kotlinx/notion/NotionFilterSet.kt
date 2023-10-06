package net.kotlinx.notion

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.core.time.toIso
import java.time.LocalDateTime

/**
 * 노션 DB 쿼리 필터 샘플
 * https://developers.notion.com/reference/post-database-query-filter
 *  */
object NotionFilterSet {

    /** 마지막 수정시간 이후  */
    fun lastEditAfter(localDateTime: LocalDateTime): ObjectType = obj {
        "and" to arr[
            obj {
                "timestamp" to "last_edited_time"
                "last_edited_time" to obj {
                    "on_or_after" to localDateTime.minusHours(9).toIso()
                }
            }
        ]
    }

}