package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * 간단한 요청정보 처리 & 리턴
 * ex) 파일 인코딩 변경
 */
class CommandDispatcher : LambdaDispatch {

    private val bus by koinLazy<EventBus>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val commandName = input[COMMAND_NAME].str ?: return null
        val event = LambdaDispatcherCommandEvent(commandName, input)
        bus.post(event)
        return event.output ?: throw IllegalStateException("이벤트 처리후 output가 등록되지 않았습니다")
    }

    companion object {

        const val COMMAND_NAME = "commandName"

    }

}

/** 간단 커맨드 이벤트 */
data class LambdaDispatcherCommandEvent(val commandName: String, val gsonData: GsonData) : AwsLambdaEvent {
    /** 결과 */
    var output: Any? = null
}


