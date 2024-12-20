package net.kotlinx.aws.bedrock

import com.lectra.koson.arr
import com.lectra.koson.obj

object LandingPageInspectionUtil {

    val PROMPT = obj {
        "role" to "광고관리자"
        "task" to "최종 광고 랜딩페이지 검수"
        "responsibilities" to arr[
            "광고페이지 주소 정상 여부 확인",
            "상품 품절 상태 확인",
            "기타 이상 여부 판단",
        ]
        "output_requirements" to arr[
            obj {
                "language" to "한글"
            },
            obj {
                "format" to "일반텍스트 없이 단일 JSON으로만 구성되어야함"
                "examples" to arr[
                    obj { "ok" to true },
                    obj {
                        "ok" to false
                        "cause" to "화면에 품절이라는 단어가 포함되어있어서 품절 상태로 간주됨"
                    },
                ]
            }
        ]
        "context" to obj {
            "date" to "2024년 12월 11일 수요일"
            "time" to "오후 4시 KST"
        }
    }

}
