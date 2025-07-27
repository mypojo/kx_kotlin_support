package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
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
        val event = CommandDispatcherEvent(commandName, input)
        bus.post(event)
        return event.output ?: throw IllegalStateException("이벤트 처리되었으나 결과파일 없음. 이벤트에서 오류 발생했는지 확인 필요")
    }

    companion object {

        const val COMMAND_NAME = "commandName"

    }

}


