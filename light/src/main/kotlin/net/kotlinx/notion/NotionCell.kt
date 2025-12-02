package net.kotlinx.notion

import com.lectra.koson.ArrayType
import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.json.gson.GsonData

/**
 * 노션 셀 변환 공통 인터페이스
 * 인간이 직접 코딩하기 위한 도구임
 * 모든 구현체는 간단 텍스트 값을 노션 입력 JSON 형태(또는 원시 타입)로 변환한다
 */
sealed interface NotionCell {

    /** notion에서 사용되는 기본 형식으로 변환 */
    fun toNotion(viewText: String): Any

    companion object {

        /** 사람이 인식 가능한 간단 텍스트로 변환해준다 */
        fun toText(body: GsonData): String {
            val type: String = body["type"].str ?: body.toPreety()
            return when (type) {

                //==================================================== 공용 ======================================================
                // 단순 스칼라 문자열인 경우 (방어적 처리)
                "url", "number", "checkbox" -> body.str ?: ""
                "text" -> body["plain_text"].str ?: ""
                "title" -> body["title"].joinToString("|") { it["plain_text"].str ?: "xx" }

                //==================================================== 데이터베이스 속성 ======================================================

                "child_database" -> body["title"].str!!  //강제로 생성한 타입!!
                "rich_text" -> body["plain_text"].str ?: ""
                "select" -> body["select"]["name"].str ?: ""
                "multi_select" -> body["multi_select"].joinToString("|") { it["name"].str!! }

                "file" -> body["name"].str!!
                "files" -> body["files"].joinToString("|") { it["name"].str ?: "" }
                "date" -> {
                    if (body["end"].empty) "${body["start"].str}"
                    else "${body["start"].str} ~ ${body["end"].str}"
                }

                else -> body.toPreety()
            }
        }
    }

    /** title 타입 */
    object Title : NotionCell {
        override fun toNotion(viewText: String): ArrayType = arr[
            obj {
                "text" to obj {
                    "content" to viewText
                }
            }
        ]
    }

    /** rich_text 타입 */
    object RichText : NotionCell {
        override fun toNotion(viewText: String): ArrayType = arr[
            obj {
                "text" to obj {
                    "content" to viewText
                }
            }
        ]
    }

    /** select 타입 */
    object Select : NotionCell {
        override fun toNotion(viewText: String): ObjectType = obj {
            "name" to viewText
        }
    }

    /** url 타입 */
    object Url : NotionCell {
        override fun toNotion(viewText: String): String = viewText
    }

    /** number 타입 */
    object NumberCell : NotionCell {
        override fun toNotion(viewText: String): Number = viewText.toLong()
    }

    /** checkbox 타입 */
    object Checkbox : NotionCell {
        override fun toNotion(viewText: String): Boolean = viewText.toBoolean()
    }

    /** date 타입 */
    object DateCell : NotionCell {
        override fun toNotion(viewText: String): ObjectType = obj {
            "start" to viewText
        }
    }

    /** 지원하지 않는 타입 (file 등) */
    object UnsupportedCell : NotionCell {
        override fun toNotion(viewText: String): Any {
            throw IllegalArgumentException("지원안함")
        }
    }

}

