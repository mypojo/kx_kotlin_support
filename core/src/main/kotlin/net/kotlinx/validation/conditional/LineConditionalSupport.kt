package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.CoroutineCondition
import com.linecorp.conditional.kotlin.coroutineCondition
import net.kotlinx.regex.extract

/**
 * 기존의 경우 true / false 를 리턴받음 -> 애매함
 * 이거 대신 예외를 던지면 실패로 인식
 *  */
fun condition(alias: String, block: suspend (ConditionContextData) -> String): CoroutineCondition {
    return coroutineCondition { ctx ->
        val messageMap = ctx.messageMap()
        val contextData = messageMap[alias]
        val successMsg = block(contextData)
        contextData.successMsg = successMsg
        contextData.failMsgs.isEmpty() //무조건 실패 메세지가 있으면 실패로 간주
    }.alias { alias }
}

/**
 * 벨리데이션 실행.
 * 주의!! 진행중인 결과를 실시간 체크해서, 실패인경우 모든 체크를 다 캔슬하고 실패를 리턴한다
 *
 * 결과객체 내부 필드를 공개하지 않아서, 일단 toString을 파싱해서 쓴다.
 * 부모 트리 등이 필요한경우 리플렉션 해서 가져오면 될듯
 *  */
suspend fun CoroutineCondition.validate(): LineConditionalResult {
    val ctx = conditionContext()
    val conditionSuccess = this.matches(ctx)
    val messageMap = ctx.messageMap()

    val eachResults = ctx.logs().map { r ->
        val text = r.toString()  //향후 수정되면 이거 바꿔야함 주의!!!
        val condition = text.extract("condition=" to ", ")!!
        val state = text.extract("state=" to ", ")!!
        val success = when (state) {
            "COMPLETED" -> text.extract("matches=" to ", ").toBoolean()
            else -> false  //CANCELLED 라면 성공실패를 체크하지 않음
        }
        val contextData = messageMap[condition]
        val logLines = when (success) {
            true -> listOf(contextData.successMsg)
            false -> contextData.failMsgs
        }
        LineConditionalResultLog(condition, success, logLines)
    }
    return LineConditionalResult(ctx, conditionSuccess, eachResults)
}


