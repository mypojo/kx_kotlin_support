package net.kotlinx.notion

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.time.toIso
import java.time.LocalDateTime

/**
 * 노션 DB 쿼리 필터 샘플
 * https://developers.notion.com/reference/post-database-query-filter
 *  */
object NotionFilterSet {

    /**
     * 간단한 and eq 조건들
     * @(property, operator, value) 이름, 타입, 값
     *  */
    fun eq(options: List<Triple<String, String, String>>): ObjectType = obj {
        "and" to arr[
            options.map {
                obj {
                    "property" to it.first
                    it.second to obj {
                        "equals" to it.third
                    }
                }
            }
        ]
    }

    /** 마지막 수정시간 이후  */
    fun lastEditAfter(localDateTime: LocalDateTime): ObjectType = obj {
        "and" to arr[
            obj {
                "timestamp" to "last_edited_time"
                "last_edited_time" to obj {
                    "on_or_after" to localDateTime.minusHours(9).toIso()
                }
            },
        ]
    }

    /** 마지막 수정시간 */
    fun lastEditBetween(between: Pair<LocalDateTime, LocalDateTime>): ObjectType = obj {
        "and" to arr[
            obj {
                "timestamp" to "last_edited_time"
                "last_edited_time" to obj {
                    "on_or_after" to between.first.minusHours(9).toIso()
                }
            },
            obj {
                "timestamp" to "last_edited_time"
                "last_edited_time" to obj {
                    "on_or_before" to between.second.minusHours(9).toIso()
                }
            },
        ]
    }

}