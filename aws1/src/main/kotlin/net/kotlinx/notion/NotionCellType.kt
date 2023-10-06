package net.kotlinx.notion

import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.core.gson.GsonData

enum class NotionCellType {

    /** title (DB 필수이름) 형식 */
    title,

    /** 일반문자 */
    rich_text,

    /** enum 형식 */
    select,

    /** 날짜 형식 */
    date,
    ;

    /** 노션 형태를 간단 텍스트로 변경 */
    fun fromNotionJson(value: GsonData): String {
        return when (this) {
            title -> value.joinToString("\n") { it["plain_text"].str ?: "" } //배열은 어떤 조건인지 확인필요
            rich_text -> value.joinToString("\n") { it["plain_text"].str ?: "" } //배열은 어떤 조건인지 확인필요
            select -> value["name"].str!!
            date -> {
                if (value["end"].empty) "${value["start"].str}"
                else "${value["start"].str} ~ ${value["end"].str}"
            }
        }
    }


    /** 노션 형태로 변경 (입력/수정) */
    fun toNotionJson(text: String): ObjectType {
        val type = this
        val typeObj = when (this) {
            title -> arr[
                obj {
                    "text" to obj {
                        "content" to text
                    }
                }
            ]

            rich_text -> arr[
                obj {
                    "text" to obj {
                        "content" to text
                    }
                }
            ]

            select -> obj {
                "name" to text
            }

            date -> obj {
                "start" to text
            }
        }
        return obj {
            "type" to type.name
            type.name to typeObj
        }
    }
}