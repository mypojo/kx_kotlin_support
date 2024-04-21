package net.kotlinx.linecorp.conditional

import com.linecorp.conditional.kotlin.CoroutineCondition
import com.linecorp.conditional.kotlin.CoroutineConditionContext
import com.linecorp.conditional.kotlin.and
import com.linecorp.conditional.kotlin.coroutineCondition
import kotlinx.coroutines.runBlocking
import net.kotlinx.core.string.ResultText
import net.kotlinx.test.TestLevel01
import net.kotlinx.test.TestRoot

/**
 * https://github.com/line/conditional
 * */
class LineConditionalTest : TestRoot() {

    val 유효성통과: CoroutineCondition = coroutineCondition { _ -> true }.alias { "유효성통과" }
    val 관리자검증: CoroutineCondition = coroutineCondition { true }.alias { "관리자검증" }
    val c: CoroutineCondition = coroutineCondition {
        it.msgs += ResultText(false,"앗! 작업 C 실패!!")
        false
    }.alias { "작업C" }


    @TestLevel01
    fun `업무로직체크`() {
        //val condition: CoroutineCondition = (a and b) and (a or c)
        val condition: CoroutineCondition = (유효성통과 and 관리자검증) and (c)

        val ctx: CoroutineConditionContext = conditionContext()

        runBlocking {
            val result = condition.matches(ctx)
            log.info { "작업결과 : $result" }
            ctx.logs().forEach {
                println(it)
            }
            log.warn { ctx.msgs }
        }
    }


}