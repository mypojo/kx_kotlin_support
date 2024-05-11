package net.kotlinx.concurrent

import net.kotlinx.collection.MapTree
import net.kotlinx.number.toRate
import net.kotlinx.string.toTextGrid
import net.kotlinx.time.TimeString
import kotlin.system.measureTimeMillis

/**
 * 단일 JVM대상으로 이미 만들어진 로직에 주입해서 병목구간을 측정하기 위한 용도
 * try - carch 로 주입해서 사용할것
 */
class StopWatch {

    /** 디버깅 후 소스코드를 제거할 필요 없이 OFF시키기만 하면 됨  */
    var on: Boolean = true

    /** 스래드 로컬 패턴이지만 static을 회피하기 위해서 멤버필드를 사용한다.  */
    private val datas: MapTree<StopWatchChecker> = MapTree { StopWatchChecker() }

    class StopWatchChecker {
        var cnt: Long = 0
        var sumOfMills: Long = 0

        fun check(duration: Long) {
            cnt++
            sumOfMills += duration
        }
    }

    /** 데이터 가져옴 (세이프하지 않음 주의) */
    fun load(): Map<String, StopWatchChecker> = datas.delegate

    fun check(name: String, block: () -> Unit) {
        val duration = measureTimeMillis(block)
        if (!on) return

        synchronized(datas) {
            datas[name].check(duration)
        }
    }

    /** 초기화도 같이 해준다.  */
    override fun toString(): String {
        synchronized(datas) {
            val totalMills = datas.delegate.entries.sumOf { it.value.sumOfMills }
            return datas.delegate.entries.map { e ->
                val checker = e.value
                val avg = checker.sumOfMills / checker.cnt
                arrayOf(
                    e.key,
                    "${checker.sumOfMills.toRate(totalMills, 1)}%",
                    checker.cnt,
                    TimeString(avg),
                )
            }.let {
                listOf("name", "비율(%)", "호출횟수", "평균처리시간").toTextGrid(it)
            }.text
        }
    }


}
