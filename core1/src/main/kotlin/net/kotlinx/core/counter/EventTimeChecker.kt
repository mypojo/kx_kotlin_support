package net.kotlinx.core.counter

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 이벤트가 발생할때마다 호출하면 각종 기능을 제공함
 * -> 에러, 장애, 경고, 캐시미스 등의 자주 일어나지 않는 이벤트를 말함

 * 최근 이벤트 발생 시간을 리턴해줌. 이걸로 딜레이 줄 수 있음
 * ex1) 오류가 많이 날때 최초 1번. 이후에는 30분에 1회만 발송
 * ex2) 로그가 많이 쌓일때 10초에 1번만 로깅
 */
class EventTimeChecker(
    private val defaultDuration: Duration = 5.seconds,
) : () -> Boolean {

    data class EventTimeResult(
        val ok:Boolean,
        /** 지금시간 */
        val now:Long,
        /** 이전 이벤트 성공시간(성공했을 경우라도) */
        val lastEvent:Long,
        /** 시도 수. 이번시도 포함. 성공이후 리셋 */
        val tryCnt:Int,
        /** 다음 hit 까지 남은시간 */
        val next:Long = 0
    )

    //=================================================== 내부사용 ===================================================
    /** 최근 이벤트 실행 시간. 0 초기화임으로 최초인경우 -> 최초 1회는 true  */
    private var lastEvent: Long = 0L
    /** 시도 수. 이번시도 포함. 성공이후 리셋 */
    private var tryCnt: Int = 0

    /** 상세 정보를 알려면 check() 사용 */
    override fun invoke(): Boolean = check().ok

    /**
     * @return 중요(기간내 최초)  이벤트인지 여부.
     * 5초로 설정해놓고 1초마다 실행시 true-false-false-false-false-true-false ...
     * !! 동기화 풀고 AtomicLong 써도 될듯.
     */
    @Synchronized
    fun check(currentInterval: Duration = defaultDuration): EventTimeResult {

        val now = System.currentTimeMillis()
        tryCnt++

        val betweenDuration = now - lastEvent
        val isMain = betweenDuration >= currentInterval.inWholeMilliseconds

        return if (isMain) {
            EventTimeResult(true,now,lastEvent,tryCnt).also {
                //hit 시 초기화 해줌
                lastEvent = now
                tryCnt = 0
            }
        }else{
            EventTimeResult(false,now,lastEvent,tryCnt,currentInterval.inWholeMilliseconds - betweenDuration)
        }
    }

    /** 메인이면 실행 */
    fun checkAnd(block: () -> Unit) {
        val main = invoke()
        if (!main) return

        block()
    }

}
