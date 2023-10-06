//package net.kotlinx.core.concurrent
//
//import net.kotlinx.core.time.TimeString
//import java.util.concurrent.TimeUnit
//import java.util.concurrent.atomic.AtomicLong
//
///**
// * HTTP를 대량으로 요청해서 합계를 측정하는 테스트기
// * 빠른 성능 측정 보다는 합계 일치가 더 중요할때 사용 (SQS,카프카 큐 누락 체크 등..)
// */
//class ThreadExecutionCounter{
//    //==================================================== 주입 ======================================================
//    private val run: Runnable? = null
//    //==================================================== 설정 ======================================================
//    /**
//     * 실행 스래드 수
//     */
//    private val threadCnt = 8
//
//    /**
//     * 요청 최대. 이 숫자에 도달하면 멈춘다.
//     */
//    private val reqMax: Long = 10000
//
//    /**
//     * 건당 누적시간, x밀리초 넘게 걸린거 카운트 가능
//     */
//    private val performanceCounter: PerformanceCounter = PerformanceCounter()
//    //==================================================== 내부사용  ======================================================
//    /**
//     * 진행중인 요청 수
//     */
//    private val reqCnt = AtomicLong()
//
//    /**
//     * 시작
//     */
//    private var startTime: Long = 0
//
//    /**
//     * 종료
//     */
//    private var endTime: Long = 0
//
//    fun startup() {
//        val threadExecutor: ThreadExecutor = ThreadExecutor.of(threadCnt)
//        val logger: ProgressLogger = ProgressLogger.of(log, reqMax)
//        val execute = Runnable {
//            while (true) {
//                val nextCnt = reqCnt.incrementAndGet()
//                if (nextCnt > reqMax) return@Runnable
//                val start = System.currentTimeMillis()
//                run!!.run()
//                val end = System.currentTimeMillis()
//                performanceCounter.check(end - start)
//                logger.log()
//            }
//        }
//        startTime = System.currentTimeMillis()
//        threadExecutor.executeAll(execute)
//        threadExecutor.shutdown()
//        endTime = System.currentTimeMillis()
//        val interval = endTime - startTime
//        val perSec: String = Decimal.parse(reqMax).div(TimeUnit.MILLISECONDS.toSeconds(interval), 2).toString()
//        log.info("전체 호출 {}건({}) -> 초당 {}건 실행", reqMax, TimeString(interval), perSec)
//        log.info(
//            " -> 건당 = 평균 {} / 최대 {} / 최소 {} : over {}",
//            TimeString(performanceCounter.getMills() / reqMax),
//            TimeString(performanceCounter.getMax()),
//            TimeString(performanceCounter.getMin()),
//            performanceCounter.getLimitCounter()
//        )
//    }
//}
