package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.file.slash
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.ResourceHolder

@OptIn(BetaOpenAI::class)
class AiModel_이미지_키워드추출 : BeSpecHeavy() {

    private val root = ResourceHolder.WORKSPACE.parentFile.slash("AI").slash("AiModel_이미지_키워드추출")


    val iamgeList = listOf(
        "https://gi.esmplus.com/pkplaza/VI/04_Event/2025/jan/samsung_salefesta_event.jpg",
        "https://cdn.011st.com/11dims/thumbnail/11src/editorImg/20240820/61647959/1724138019889_E.jpg",
    )

    private val imageFile = iamgeList.map { AiTextInput.AiTextImage { url = it } }

    init {
        initTest(KotestUtil.IGNORE)

        Given("LLM 이미지 모델 비교") {

            val set = AiModelLocalSet {
                aws = aws97
                this.systemPrompt = SYSTEM_PROMPT_GPTO1_MINI
                clients = listOf(
                    gpt4O,
                    gpt4OMini, //가끔 품절 아닌데 품절로 뜸
                    //claudeSonet,  //잘되는데 비쌈
                    //claudeHaiku, //프롬프트 인식 불가
                    //deepseek  //에러남
                )
            }


            Then("모델별 비교분석") {
                val results = imageFile.flatMap { input ->
                    set.clients.map { client ->
                        suspend {
                            //퍼블렉시티의 경우 파일만 첨부가 불가능. 반드시 텍스트가 같이 입력되어야함
                            client.text(listOf(input)).also {
                                it.name = input.file?.name ?: "-"
                            }
                        }
                    }
                }.coroutineExecute()
                results.printSimple()
            }
        }
    }

}

/** 기본 시스템 프롬프트 */
val SYSTEM_PROMPT_GPTO1_MINI = obj {

    //==================================================== 요구사항 ======================================================
    "systemPrompt" to obj {
        "role" to "키워드광고 전문가"
        "task" to "주어진 상품 정보를 사용해서 대한민국의 검색 키워드 광고에 적합한 일반키워드와 메인키워드 추출"
        "requirements" to obj {
            "normal" to "일반키워드 : 일반적으로 많이 검색하는 키워드 이나 구매 전환율이 높지 않은 키워드로 상품당 5개 추출"
            "main" to "메인키워드 : 상품 키워드와 브랜드 or 카테고리 등을 조합하는등, 전환율이 높아 광고에 적합한 키워드로 상품당 10개 이상 추출"
            "output_rules" to arr[
                //기본 요구사항
                "결과는 특수문자와 공백문자가 없는 한글 및 영문대문자로만 구성",
                "입력 상품수와 출력 상품수는 반드시 일치해야함",
                "결과는 설명문구 없이 반드시 하나의 단일 JSON 으로 리턴",
                "입력값에 이미지 URL이 첨부되는경우, 별도의 이미지로 입력됨",

                //프롬프트 팁
                "브랜드명이 있는경우, 가능하면 키워드에 포함시켜",
                "모델명이 있는경우, 가능하면 키워드에 포함시켜",
                "영문이 포함되어있는경우 자연스러운 한국어로 변형해서 키워드에 포함시켜",
            ]
        }
    }

    //==================================================== 포맷 ======================================================
    "format" to obj {
        "input_format" to obj {
            "type" to "json array"
        }
        "output_format" to obj {
            "type" to "json"
            "structure" to obj {
                "result" to arr[
                    obj {
                        "id" to "string"
                        "keywords" to obj {
                            "normal" to arr["string"]
                            "main" to arr["string"]
                        }
                    }
                ]
            }
        }
        "example" to obj {
            "input" to arr[
                obj {
                    "id" to "123"
                    "title" to "프라다 PRINT DRAPE 미니 버킷백"
                    "maker" to "PRADA"
                    "category1" to "여성의류"
                    "category2" to "어린이"
                },
                obj {
                    "id" to "124"
                    "title" to "[그리디어스] PRINT DRAPE SKIRT 2VH157 2FOQ F0L8P "
                    "maker" to "GREEDILOUS"
                    "category1" to "여성의류"
                    "category2" to "스커트"
                },
            ]
            "output" to obj {
                "result" to arr[
                    obj {
                        "id" to "123"
                        "keywords" to obj {
                            "normal" to arr["어린이버킷백", "키즈백팩", ".."]
                            "main" to arr["프라다어린이버킷백", "프라다여성미니가방", ".."]
                        }
                    },
                    obj {
                        "id" to "124"
                        "keywords" to obj {
                            "normal" to arr["그리디어스", "2VH1572FOQF0L8P", "그리디어스스커트", "그리디어스치마"]
                            "main" to arr["그리디어스프린트스커트", "그리디어스드레이프스커트", "그리디어스프린트치마", "2VH1572FOQ", "미디치마", ".."]
                        }
                    },
                ]
            }
        }
    }
}.toGsonData()