package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.*
import jakarta.validation.ValidationException
import net.kotlinx.collection.MapTree
import net.kotlinx.regex.RegexParseSupport

private const val MESSAGE_MAP = "messageMap"

/**
 * 간단 메세지 컨텍스트 생성
 * https://github.com/line/conditional
 * */
fun conditionContext(): CoroutineConditionContext {
    val tree: MapTree<MutableList<String>> = MapTree { mutableListOf<String>() }
    return coroutineConditionContext(
        MESSAGE_MAP to tree,
    )
}

/** 간단 메세지 컨텍스트 접근 */
val CoroutineConditionContext.messageMap: MapTree<MutableList<String>>
    get() = this[MESSAGE_MAP] as MapTree<MutableList<String>>


/**
 * 기존의 경우 true / false 를 리턴받음
 * 이거 대신 예외를 던지면 실패로 인식
 *  */
fun condition(alias: String, block: suspend (ctx: MutableList<String>) -> Unit): CoroutineCondition {
    return coroutineCondition { ctx ->
        val msgs = ctx.messageMap[alias]
        try {
            block(msgs)
            true
        } catch (e: ValidationException) {
            msgs += e.message ?: e::class.simpleName!!
            false
        }
    }.alias { alias }
}

/**
 * CoroutineConditionMatchResult 가 다 private 이다.. 왜지???
 * 이때문에 따로 만든다.
 *  */
data class ConditionResult(
    val condition: String,
    val matches: Boolean,
    val message: List<String>,
)

fun CoroutineConditionContext.resultLogs(): List<ConditionResult> = this.logs().map { r ->
    val text = r.toString()  //향후 수정되면 이거 바꿔야함 주의!!!
    val failMap = this.messageMap
    RegexParseSupport.template {
        val condition = text.extract("condition=" to ", ")!!
        val matches = text.extract("matches=" to ", ")!!.toBoolean()
        ConditionResult(condition, matches, failMap[condition])
    }
}

