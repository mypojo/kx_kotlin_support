package net.kotlinx.aws.lambdaCommon

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaHandlerUtil
import net.kotlinx.core.Kdsl
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.core.time.TimeUtil
import org.crac.Resource

/**
 * 하나만 만들어서 여러군데 다 사용하고싶은 범용 람다 함수  핸들러
 * Core.getGlobalContext().register(this) 해줄것!
 */
abstract class CommonFunctionHandler : RequestHandler<Map<String, Any>, Map<String, Any>>, Resource {

    protected val log = KotlinLogging.logger {}

    /** 전체 내장 로직. 한번 람다 호출에 한개만 동작한다 */
    protected val logics: MutableList<LambdaFunctionLogic> = mutableListOf()

    /** snapstart 등에서 리셋 */
    fun clear(){
        logics.clear()
    }

    /** 로직 등록 */
    @Kdsl
    fun regtiter(block: LambdaFunctionLogic.() -> Unit = {}) {
        logics += LambdaFunctionLogic().apply(block)
    }

    /** 예외 처리 핸들러 */
    lateinit var errorhandler: (GsonData, Throwable) -> Map<String, Any>

    /**
     * 각종 기능을 순서대로 체크 후 실행한다.
     * */
    override fun handleRequest(event: Map<String, Any>, context: Context?): Map<String, Any> {
        return runBlocking {
            val data = GsonData.fromObj(event)

            try {//sns의 경우 여러개 입력 될 수 있음으로 각각 호출
                val records = data["Records"]
                if (records.empty) {
                    return@runBlocking doHandleRequest(data, context)
                } else {
                    val results = records.map { doHandleRequest(it, context) }
                    return@runBlocking results.first() //아무거나 하나만 리턴(의미없음)
                }
            } catch (e: Throwable) {
                return@runBlocking errorhandler(data, e)
            }
        }
    }

    private suspend fun doHandleRequest(data: GsonData, context: Context?): Map<String, Any> {
        try {
            for (logic in logics) {
                log.trace { "람다로직 체크 ${logic.handler::class.simpleName} ..." }
                val result = logic.handler(data, context) ?: continue

                log.info { "람다로직 종료 ${logic.handler::class.simpleName} 적용" }
                val lambdaMap = LambdaHandlerUtil.anyToLambdaMap(result)
                log.debug { " -> 결과 $lambdaMap" }
                return lambdaMap
            }
        } catch (e: Throwable) {
            //오류 발생시에만 data 로그 출력
            log.warn { "오류!! ${e.toSimpleString()} -> 입력이벤트출력 $data" }
            throw e
        }
        throw IllegalStateException("매칭되는 로직이 없습니다! $data")
    }


    /** 스냅스타트 초기화 */
    override fun beforeCheckpoint(context: org.crac.Context<out Resource>) {
        log.info { "AWS snapstart beforeCheckpoint.." }
        runBlocking {
            logics.forEach {
                log.info { " -> snapstart logic ${it.handler::class.simpleName} init" }
                it.snapStart?.invoke()
            }
            beforeCheckpointFinally()
        }
    }

    /** 스냅스타트 이후 DI 정보를 다시 복구시킨다 */
    abstract suspend fun beforeCheckpointFinally()

    /** 스탭스타트 복구 */
    override fun afterRestore(context: org.crac.Context<out Resource>?) {
        log.info { "AWS snapstart afterRestore" }
    }

    init {
        TimeUtil.initTimeZone()
        //아직 셧다운 훅은 지원안함.
        Runtime.getRuntime().addShutdownHook(Thread {
            log.warn { "### 람다가 셧다운 됩니다 ###" }
            ResourceHolder.finish()
        })
    }
}
