package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagentruntime.model.OrchestrationTrace
import aws.sdk.kotlin.services.bedrockagentruntime.model.PostProcessingTrace
import aws.sdk.kotlin.services.bedrockagentruntime.model.ResponseStream
import aws.sdk.kotlin.services.bedrockagentruntime.model.Trace
import mu.KotlinLogging
import net.kotlinx.exception.toSimpleString
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.json
import net.kotlinx.json.gson.toGsonArray
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.reflect.name

/**
 * 디버깅용 간단 텍스트로 변환
 * 기본 객체는 secret 적용되서 toString 이 안됨. 강제로 파싱해서 보여줌
 * */
class BedrockTrace(private val root: ResponseStream.Trace) {

    val part = root.value
    val trace = part.trace!!

    companion object {
        private val log = KotlinLogging.logger {}
    }

    // 모든 케이스에서 title 이 설정되도록 선계산 (불필요한 예외 처리는 제거)
    val title: String = when (trace) {
        is Trace.OrchestrationTrace -> {
            when (trace.value) {
                is OrchestrationTrace.ModelInvocationInput -> "OrchestrationTrace.ModelInvocationInput (오케스트레이션-모델입력)"
                is OrchestrationTrace.ModelInvocationOutput -> "OrchestrationTrace.ModelInvocationOutput (오케스트레이션-모델출력)"
                is OrchestrationTrace.Rationale -> "OrchestrationTrace.Rationale (오케스트레이션-추론)"
                is OrchestrationTrace.InvocationInput -> "OrchestrationTrace.InvocationInput (오케스트레이션-호출입력)"
                is OrchestrationTrace.Observation -> "OrchestrationTrace.Observation (오케스트레이션-관찰)"
                else -> "확인필요! -> ${trace::class.simpleName} -> ${trace.value::class.simpleName}"
            }
        }

        is Trace.PostProcessingTrace -> {
            when (trace.value) {
                is PostProcessingTrace.ModelInvocationInput -> "PostProcessingTrace.ModelInvocationInput (후처리-모델입력)"
                is PostProcessingTrace.ModelInvocationOutput -> "PostProcessingTrace.ModelInvocationOutput (후처리-모델출력)"
                else -> throw IllegalArgumentException("지원하지 않는 타입 : ${trace.value::class.qualifiedName}")
            }
        }

        else -> "확인필요! -> ${trace::class.simpleName}"
    }

    val data: GsonData = try {
        when (trace) {  //실제 트레이스는 래핑되어있음
            is Trace.OrchestrationTrace -> {
                // 한 단계 더 들어가서 가능한 경우 모델 입력 텍스트를 보여줌
                when (val inner = trace.value) {

                    is OrchestrationTrace.ModelInvocationInput -> inner.value.text!!.toGsonData()

                    is OrchestrationTrace.ModelInvocationOutput -> inner.value.rawResponse!!.content!!.toGsonData()

                    is OrchestrationTrace.Rationale -> inner.value.text!!.toGsonData()

                    is OrchestrationTrace.InvocationInput -> GsonData.fromObj(inner.value)

                    //수정 필요함! 일단 임시조치
                    is OrchestrationTrace.Observation -> inner.value.knowledgeBaseLookupOutput?.retrievedReferences?.map { GsonData.fromObj(it) }?.toGsonArray() ?: GsonData.empty()

                    else -> {
                        log.warn { "지원하지 않는 타입 ${title} => 강제변환" }
                        GsonData.fromObj(trace.value)
                    }
                }
            }

            is Trace.PostProcessingTrace -> {
                log.debug { " -> 데이터 시리얼..  ${trace.value::class.name()}" }
                when (val inner = trace.value) {

                    is PostProcessingTrace.ModelInvocationInput -> GsonData.fromObj(inner.value)

                    is PostProcessingTrace.ModelInvocationOutput -> {
                        val obj = inner.value
                        obj.rawResponse!!.content!!.toGsonData() //gson 변경 에러나서, 일단 부분만 사용
                    }

                    else -> throw IllegalArgumentException("지원하지 않는 타입 : ${trace.value::class.qualifiedName}")
                }
            }

            else -> json {
                "name" to trace::class.simpleName
                "msg" to "Trace 확인필요!!"
            }
        }
    } catch (e: Exception) {
        log.warn { "[${title}] 데이터 파싱 실패.. ${e.toSimpleString()}" }
        GsonData.empty()
    }

}