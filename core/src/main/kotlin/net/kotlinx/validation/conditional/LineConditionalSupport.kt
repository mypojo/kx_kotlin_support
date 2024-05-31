package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.CoroutineCondition
import com.linecorp.conditional.kotlin.CoroutineConditionContext
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
 * 벨리데이션 실행
 *  */
suspend fun CoroutineCondition.validate(): ConditionResult {
    val ctx = conditionContext()
    val conditionSuccess = this.matches(ctx)
    val messageMap = ctx.messageMap()

    /** 내부 필드를 공개하지 않아서, toString을 파싱해서 쓴다. */
    val eachResults = ctx.logs().map { r ->
        val text = r.toString()  //향후 수정되면 이거 바꿔야함 주의!!!
        val condition = text.extract("condition=" to ", ")!!
        val matches = text.extract("matches=" to ", ")!!.toBoolean()
        val contextData = messageMap[condition]
        val logLines = when (matches) {
            true -> listOf(contextData.successMsg)
            false -> contextData.failMsgs
        }
        ConditionResultLog(condition, matches, logLines)
    }
    return ConditionResult(ctx, conditionSuccess, eachResults)
}


data class ConditionResult(
    /** context */
    val ctx: CoroutineConditionContext,
    /** 전체 성공 여부. */
    val ok: Boolean,
    /** 상세 로그 결과 */
    val logs: List<ConditionResultLog>,
)

/**
 * CoroutineConditionMatchResult 가 다 private 이다.. 왜지???
 * 이때문에 따로 만든다.
 *  */
data class ConditionResultLog(
    val condition: String,
    val matches: Boolean,
    val message: List<String>,
)