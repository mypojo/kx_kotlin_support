package net.kotlinx.notion

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.json.gson.GsonData

/**
 * enum 과 실제 타입 이름을 맞추려는 무리수로 이렇게 됨.. ㅠㅠ
 * 일단 Suppress 로 해결.
 * */
@Suppress("EnumEntryName")
enum class NotionCellType {

    /** title (DB 필수이름) 형식 */
    title,

    /** 일반문자 */
    rich_text,

    /** enum 형식 */
    select,

    /** 날짜 형식 */
    date,

    /** number 형식 */
    number,

    /** url 형식 */
    url,

    /** checkbox 형식 */
    checkbox,

    /** file 형식 */
    file,
    ;

    /**
     * 노션 형태를 간단 텍스트로 변경
     * https://developers.notion.com/reference/page-property-values#date
     *  */
    fun fromNotionJson(value: GsonData): String {
        if (value.empty) return ""

        return when (this) {
            title -> value.joinToString("\n") { it["plain_text"].str ?: "" } //배열은 어떤 조건인지 확인필요
            rich_text -> value.joinToString("\n") { it["plain_text"].str ?: "" } //배열은 어떤 조건인지 확인필요
            select -> value["name"].str!!
            url -> value.str!! //그 자체
            number -> value.str!! //그 자체
            checkbox -> value.str!! //그 자체
            file -> value.str!! //그 자체 (다운로드?)
            date -> {
                if (value["end"].empty) "${value["start"].str}"
                else "${value["start"].str} ~ ${value["end"].str}"
            }
        }
    }


    /** 노션 형태로 변경 (입력/수정) */
    fun toNotionJson(text: String): Any {
        val type = this
        return when (type) {
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

            url -> text

            number -> text.toLong()

            checkbox -> text.toBoolean()

            file -> throw IllegalArgumentException("지원안함")

            date -> obj {
                "start" to text
            }
        }
    }

//        return obj {
//            "type" to type.name
//            type.name to typeObj
//        }
//    }
}