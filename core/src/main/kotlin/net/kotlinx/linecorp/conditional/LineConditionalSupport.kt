package net.kotlinx.linecorp.conditional

import com.linecorp.conditional.kotlin.CoroutineConditionContext
import com.linecorp.conditional.kotlin.coroutineConditionContext
import com.linecorp.conditional.kotlin.get
import net.kotlinx.core.string.ResultText

private const val MSG = "msg"

/**
 * 간단 메세지 컨텍스트 생성
 * https://github.com/line/conditional
 * */
fun conditionContext(): CoroutineConditionContext {
    val msgList = mutableListOf<ResultText>()
    return coroutineConditionContext(MSG to msgList)
}

/** 간단 메세지 컨텍스트 접근 */
val CoroutineConditionContext.msgs: MutableList<ResultText>
    get() = this[MSG] as MutableList<ResultText>
