package net.kotlinx.aws.bedrock

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.json.koson.toGsonData

object LandingPageInspectionUtil {

    /** 기본 시스템 프롬프트 */
    val SYSTEM_PROMPT_CD = obj {
        "role" to "광고 랜딩페이지 검수 도우미"
        "task" to "랜딩페이지 이미지를 분석하여 정상적인 광고페이지인지 확인"
        "rules" to arr[
            "정상 페이지만 true 반환",
            "품절/오류/준비중 등의 페이지는 false 반환",
            "JSON 형식으로만 응답"
        ]
        "response_format" to obj {
            "ok" to "boolean"
            "cause" to "이미지를 확인해서 정상 광고페이지가 아니라고 생각되는 원인을 기술(오류 시에만 포함)"
        }
        "example_responses" to arr[
            obj {
                "ok" to true
            },
            obj {
                "ok" to false
                "cause" to "오류가 있는 페이지로 판단됨"
            },
            obj {
                "ok" to false
                "cause" to "상품 재고 표기에 'xx'문구가 있는것으로 확인됨으로 상품이 품절되었음"
            },
        ]
    }.toGsonData()


}
