package net.kotlinx.ai.demo.tnd

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.json.koson.toGsonData

object DemoTndUtil {

    /** 기본 시스템 프롬프트 */
    val SYSTEM_PROMPT = obj {
        "task" to "주어진 광고 사이트 URL 및 가이드를 참고해서 광고 제목(title)과 설명(desc)을 도출"
        "rules" to arr[
            "제목은 최대 15자로 제한",
            "설명은 가능하면 30자 이상으로 구성해주고, 반드시! 최대 45자를 넘지 않도록 해줘",
            "제목에는 제공하는 상품이나 서비스가 정확하게 명시 되어야해",
            "설명에는 가능하면 광고적으로 수식할 수 있는 문구로 만들어줘 ex) '손쉬운' -> '한방에 해결되는'",
            "제목 생성시 입력된 json의 'title_guide' 를 고려해서 작성해줘",
            "설명 생성시 입력된 json의 'desc_guide' 를 고려해서 작성해줘",
            "제목은 2개,  설명은 5개 만들어줘", // 저렴한 모델의경우 결과가 많아지면 정신 못차림
            "다른 텍스트 없이 반드시 단일 JSON object 형식으로만 응답해줘",
        ]
        "example_request" to obj {
            "site" to "https://only.webhard.co.kr/"
            "title_guide" to "LG유플러스 공식사이트를 강조"
            "desc_guide" to "저렴한 요금제와 제품의 특장점을 강조"
        }
        "example_responses" to obj {
            "title" to arr[
                "LG유플러스 공식 웹하드",
                "LG유플러스의 공식 웹하드",
            ]
            "desc" to arr[
                "저렴한 요금제와 안정적인 보안 기능으로 LG유플러스 공식 웹하드 서비스를 만나보세요",
                "안정적인 인터넷과 IoT 기술로 LG유플러스의 혁신적인 서비스를 경험하세요",
                "고객 중심 서비스와 5G 기술로 강화된 웹하드 보안을 제공해드립니다",
            ]
        }

//        title {keyword:입점 브랜드명} X 디X롯 할인
//                desc
//        * 더 나은 삶을 제안하는 플랫폼. 새로운 브랜드와 아이디어의 발견
//        * 작은 변화로 행복해지는 소소한 순간 {keyword:브랜드명}, 디플롯에서 ~60% 할인!
    }.toGsonData()


}