package net.kotlinx.openAi.kwdExtract

import com.lectra.koson.arr
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.exception.KnownException

object KwdExtractUtil {

    /** 기본 시스템 프롬프트 */
    val SYSTEM_PROMPT = obj {
        "role" to "keyword_analysis_expert"
        "task" to "주어진 상품 정보를 바탕으로 검색 키워드 광고에 적합한 주요(main) 광고 키워드와 세부(sub) 광고키워드 추출"
        "input_format" to obj {
            "type" to "csv"
            "headers" to arr["상품ID", "상품명", "테마", "상품URL"]
        }
        "output_format" to obj {
            "type" to "json"
            "structure" to obj {
                "result" to arr[
                    obj {
                        "item_id" to "string"
                        "keywords" to obj {
                            "main" to arr["string"]
                            "sub" to arr["string"]
                        }
                    }
                ]
            }
        }
        "requirements" to obj {
            "main_keyword_item" to "주요키워드는 광고비가 높고 중요한 키워드로 상품당 최소 4개, 최대 8개"
            "sub_keyword_item" to "세부 키워드는 자주 사용되는 키워드는 아니지만, 가격대비 효율이 좋은 키워드로 상품당 최소 10개, 최대 20개"
            "output_rules" to arr[
                "입력 상품수와 출력 상품수는 반드시 일치해야함",
                "1.5kg 등의 단위 표시는 광고키워드에 적합하지 않아",
                "검색광고에 유용한 키워드를 포함해야함",
                "결과는 반드시 JSON 형식으로 리턴되어야함",
            ]
            "keyword_rules" to arr[
                "가능하면 한글 키워드 사용",
            ]
            "sorting" to "예상 광고비용 높은 순으로 정렬"
        }
        "example" to obj {
            "input" to "1,오뚜기_스파게티_500G, 스파게티 50% 할인"
            "output" to obj {
                "result" to arr[
                    obj {
                        "item_id" to "1"
                        "keywords" to obj {
                            "main" to arr["오뚜기스파케티", "맛있는스파게티", ".."]
                            "sub" to arr["스파게티할인", ".."]
                        }

                    }
                ]
            }
        }
        // 추가 제약조건
        "keyword_constraints" to obj {
            "min_keyword_length" to 2
            "max_keyword_length" to 20
            "forbidden_characters" to arr["!", "@", "#", "$", "%", "{", "}"]
            "text_format" to obj {
                "english" to "UPPERCASE_ONLY"
                "space" to "NO_WHITESPACE"
                "case_examples" to arr[
                    obj { "Samsung" to "SAMSUNG" },
                    obj { "iPhone" to "IPHONE" },
                ]
            }
        }
    }.toString()

    private val log = KotlinLogging.logger {}

    fun validate(ins: List<KwdExtractIn>, outs: List<KwdExtractOut>) {
        val exKwdCnt = outs.sumOf { it.mains.size + it.subs.size }
        if (ins.size == outs.size) {
            log.info { "입력키워드 ${ins.size} / 출력키워드 ${outs.size} -> ${exKwdCnt}건 추출" }
        } else {
            log.warn { "입력키워드 ${ins.size} / 출력키워드 ${outs.size} -> ${exKwdCnt}건 추출" }
            log.warn { " => in  ${ins.map { it.itemId }}" }
            log.warn { " => out ${outs.map { it.itemId }}" }
            throw KnownException.ItemRetryException("입력 / 출력 키워드 불일치")
        }
    }

}

