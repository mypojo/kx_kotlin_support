package net.kotlinx.notion

import com.lectra.koson.ArrayType
import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import mu.KotlinLogging

/**
 * 워낙 바리에이션이 많아서, 자주 사용하는거만 등록
 * */
object NotionCellBuilder {

    private val log = KotlinLogging.logger {}

    fun richText(viewText: String): ArrayType {
        return arr[
            obj {
                "text" to obj {
                    "content" to viewText
                }
            }
        ]
    }

    fun select(viewText: String): ObjectType {
        return obj {
            "name" to viewText
        }
    }

    /** notion 입력폼으로 변경해줌 */
    fun toJson(type: String, viewText: String): Any = when (type) {
        "title" -> arr[
            obj {
                "text" to obj {
                    "content" to viewText
                }
            }
        ]

        "rich_text" -> arr[
            obj {
                "text" to obj {
                    "content" to viewText
                }
            }
        ]

        "select" -> obj {
            "name" to viewText
        }

        "url" -> viewText

        "number" -> viewText.toLong()

        "checkbox" -> viewText.toBoolean()

        "file" -> throw IllegalArgumentException("지원안함")

        "date" -> obj {
            "start" to viewText
        }

        else -> throw IllegalArgumentException("$type is not rquired")
    }

}

