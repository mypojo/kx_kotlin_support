package net.kotlinx.calculator

import net.kotlinx.counter.EventTimeChecker
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * 단일 JVM에서 진행상황을 대략적으로 로깅을 하려고 할때 사용
 * ProgressData 를 내부적으로 사용함
 */
class ProgressInlineChecker(
    /** 전체 수  */
    val total: Long,
    /** X시간당 한번만 로깅  */
    private var eventTimeChecker: EventTimeChecker = EventTimeChecker(),
) {

    //============================= 내부사용 =====================================
    private val counter: AtomicLong = AtomicLong()
    private val progressStartTime = LocalDateTime.now()

    /** 단일 JVM에서 진행할때 사용 */
    fun check(delta: Long = 1, block: (ProgressData) -> Unit = { println(it) }) {

        check(delta > 0)

        val isFirst = counter.get() == 0L
        val completed = counter.addAndGet(delta)
        val isLast = completed == total

        if (!eventTimeChecker() && !isLast) return  //간격 임계치 미만이라면 로깅 스킵. 마지막 라인만 빼고~
        if (isFirst) return  //처음이면 스킵. 너무 빨리 시간체크가 되서 남은 시간이 너무 많아보이게 되는거 방지

        block(
            ProgressData(total, completed, progressStartTime)
        )
    }

//    companion object {
//        val DEFAULT_LOGGER: (ProgressData) -> Unit = {
//            //println(" => ${it.completed} / ${it.total} (${it.progressRate}%) [진행시간 ${it.progressTime.toTimeString()}] [남은예상시간 ${it.remainTime.toTimeString()}]")
//            println(it)
//        }
//    }

}

