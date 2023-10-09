package net.kotlinx.aws.lambdaCommon

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaHandlerUtil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.core.time.TimeUtil
import org.crac.Resource

/**
 * 하나만 만들어서 여러군데 다 사용하고싶은 범용 람다 함수  핸들러
 * Core.getGlobalContext().register(this) 해줄것!
 */
@LambdaFunctionLogicDsl
abstract class CommonFunctionHandler : RequestHandler<Map<String, Any>, Map<String, Any>>, Resource {

    protected val log = KotlinLogging.logger {}

    /** 전체 내장 로직. 한번 람다 호출에 한개만 동작한다 */
    private val logics: MutableList<LambdaFunctionLogic> = mutableListOf()

    /** 로직 등록 */
    fun regtiter(block: LambdaFunctionLogic.() -> Unit = {}) {
        logics += LambdaFunctionLogic().apply(block)
    }

    /**
     * 각종 기능을 순서대로 체크 후 실행한다.
     * */
    override fun handleRequest(event: Map<String, Any>, context: Context?): Map<String, Any> {
        return runBlocking {
            val data = GsonData.fromObj(event)
            for (logic in logics) {
                log.debug { "람다로직 ${logic.id} : $event -> 실행..." }
                val result = logic.handler(data, context) ?: continue
                log.info { "람다로직 ${logic.id} : $event -> $result" }
                return@runBlocking LambdaHandlerUtil.anyToLambdaMap(result)
            }
            throw IllegalStateException("매칭되는 로직이 없습니다! $event")
        }
    }


    /** 스냅스타트 초기화 */
    override fun beforeCheckpoint(context: org.crac.Context<out Resource>) {
        log.info { "AWS snapstart beforeCheckpoint.." }
        runBlocking {
            logics.forEach {
                log.info { " -> snapstart logic ${it.id} init" }
                it.snapStart?.invoke()
            }
        }
    }

    /** 스탭스타트 복구 */
    override fun afterRestore(context: org.crac.Context<out Resource>?) {
        log.info { "AWS snapstart afterRestore" }
    }

    init {
        TimeUtil.initTimeZone()
        //아직 셧다운 훅은 지원안함.
        Runtime.getRuntime().addShutdownHook(Thread {
            log.warn("### 람다가 셧다운 됩니다 ###")
            ResourceHolder.finish()
        })
    }
}
