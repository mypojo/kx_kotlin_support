package net.kotlinx.core2.concurrent

/**
 * 단일 스래드에서 일정 주기만큼 주기적으로 슬립할때 사용
 * 최소 시간을 정해놓고, 해당 최소 시간 이하로 다음 체크가 온다면 최소 시간에 맞게 슬립해준다.
 * ex) 10분을 지정했을때, 이전 처리에 2분이 소모되어서 다음 체크가 8분 후라면  8분간 슬립
 *
 * 첫 체크는 슬립하지않고 통과함으로 for문의 맨 앞에 놓을것.
 */
class ThreadSleepTool(
    private val minInterval: Long,
) {

    private var before: Long = 0

    /**
     * 랜덤 설정이 있을때는 슬립 시간을 랜덤하게 준다.
     *  */
    @Throws(InterruptedException::class)
    fun checkAndSleep() {
        if (before != 0L) {
            val interval = System.currentTimeMillis() - before
            var currentInterval = minInterval
            val sleep = currentInterval - interval
            if (sleep > 0) Thread.sleep(sleep)
        }
        before = System.currentTimeMillis()
    }

}