package net.kotlinx.aws.lambda.dispatch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import com.lectra.koson.ObjectType
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaHandlerUtil
import net.kotlinx.aws.lambda.dispatch.asynch.AwsCodeDeployHookPublisher
import net.kotlinx.aws.lambda.dispatch.asynch.AwsEventBridgePublisher
import net.kotlinx.aws.lambda.dispatch.asynch.AwsSnsPublisher
import net.kotlinx.aws.lambda.dispatch.asynch.AwsSqsPublisher
import net.kotlinx.aws.lambda.dispatch.synch.*
import net.kotlinx.core.Kdsl
import net.kotlinx.domain.job.JobEventBridgePublisher
import net.kotlinx.exception.toSimpleString
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.okhttp.OkHttpMediaType


/**
 * 모든 요청을 한곳에서 디스패치함
 *
 * 1. 동기 로직 (결과 리턴이 필요한것) = ex) SFN 결과 (다음로직에서 사용), HTML UI , API json 응답
 * 2. 비동기 가능 로직 (결과 리턴이 필요하지 않은것) = ex) SNS알림, S3데이터 처리요청 등
 *
 * 최초 생성 -> 스냅스타트 초기화로직 실행 ->새로 생성후 스냅스타트 이미지 생성 시키기
 */
class LambdaDispatcher {

    private val log = KotlinLogging.logger {}

    @Kdsl
    constructor(block: LambdaDispatcher.() -> Unit = {}) {
        apply(block)
    }

    /** 메인 이벤트버스 */
    private val bus by koinLazy<EventBus>()

    /**
     * 디폴트 내장로직이 이미 추가되어있음.
     * */
    var logics: List<LambdaDispatch> = listOf(
        //==================================================== asynch (guava eventbus 호출) ======================================================
        AwsCodeDeployHookPublisher(),
        AwsEventBridgePublisher(),
        AwsSnsPublisher(),
        AwsSqsPublisher(),
        JobEventBridgePublisher(),

        //==================================================== synch (런타임 직접 실행) ======================================================
        CommandDispatcher(),
        BatchStepDispatcher(),
        JobDispatcher(),
        KtorDispatcher(),
        S3LogicDispatcher(),
        BedrockActionGroupDispatcher(),
    )

    /**
     * 디폴트 컨버터
     * */
    var toMapConverter: (Any) -> Map<String, Any> = { result ->
        when (result) {

            is LambdaDispatcherFailEvent -> {
                mapOf(
                    "statusCode" to 500,
                    "headers" to mapOf(
                        "content-type" to OkHttpMediaType.JSON
                    ),
                    "body" to mapOf(
                        "error" to result.e.toSimpleString(),
                        "stackTrace" to result.e.stackTraceToString(),
                    ),
                )
            }

            else -> {
                LambdaHandlerUtil.anyToLambdaMap(result)
            }
        }
    }

    /** 테스트 실행용 메소드 */
    suspend fun handleRequest(event: ObjectType): Map<String, Any> = handleRequest(event.toGsonData().fromJson<Map<String, Any>>(), null)

    /**
     * 실제 요청 핸들
     * */
    suspend fun handleRequest(event: Map<String, Any>, context: Context?): Map<String, Any> {
        val data = GsonData.fromObj(event)
        val result = try {//sns의 경우 여러개 입력 될 수 있음으로 각각 호출
            val records = data["Records"]
            if (records.empty) {
                handleRequestEach(data, context)
            } else {
                val results = records.map { handleRequestEach(it, context) }
                results.first() //아무거나 하나만 리턴(의미없음)
            }
        } catch (e: Throwable) {
            log.warn { "오류!! ${e.toSimpleString()} -> 입력이벤트 = $data" }
            bus.postEvent { LambdaDispatcherFailEvent(data, e) }
            throw e  //SFN 등에서 명시적으로 실패(빨간줄)가 되도록 예외를 던진다. 예외가 던져지면 안되는 일부 로직(S3Logic)등은 따로 처리할것
        }
        return toMapConverter(result)
    }

    private suspend fun handleRequestEach(data: GsonData, context: Context?): Any {
        for (logic in logics) {
            log.trace { "람다로직 체크 ${logic::class.simpleName} ..." }
            val result = logic.postOrSkip(data, context) ?: continue

            log.info { "람다로직 종료 ${logic::class.simpleName} 적용됨" }
            return result
        }
        return bus.postEvent { LambdaDispatcherDeadEvent(data) }
    }


}
