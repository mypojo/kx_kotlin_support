package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.CoroutineConditionContext
import com.linecorp.conditional.kotlin.coroutineConditionContext
import com.linecorp.conditional.kotlin.get
import net.kotlinx.collection.MapTree

private const val MESSAGE_MAP = "messageMap"

class ConditionContextData {

    /** 실패 메세지 (많을 수 있음) */
    val failMsgs: MutableList<String> = mutableListOf()

    /** 성공 메세지 */
    var successMsg: String = "-"

}

/**
 * 간단 메세지 컨텍스트 생성
 * https://github.com/line/conditional
 * */
fun conditionContext(): CoroutineConditionContext {
    val tree: MapTree<ConditionContextData> = MapTree { ConditionContextData() }
    return coroutineConditionContext(
        MESSAGE_MAP to tree,
    )
}

/** 간단 메세지 컨텍스트 접근 */
fun CoroutineConditionContext.messageMap(): MapTree<ConditionContextData> = this[MESSAGE_MAP] as MapTree<ConditionContextData>

