package net.kotlinx.core2.concurrent

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong

/**
 * 주기만큼 주기적으로 슬립할때 사용
 * 최소 시간을 정해놓고, 해당 최소 시간 이하로 다음 체크가 온다면 최소 시간에 맞게 슬립해준다.
 * ex) 10분을 지정했을때, 이전 처리에 2분이 소모되어서 다음 체크가 8분 후라면  8분간 슬립
 *
 * 첫 체크는 슬립하지않고 통과함으로 for문의 맨 앞에 놓을것.
 */
class CoroutineSleepTool(
    private val minInterval: Long,
) {

    private var before: Long = 0
    val cnt:AtomicLong = AtomicLong()

    suspend fun checkAndSleep() {
        cnt.incrementAndGet()
        if (before != 0L) {
            val interval = System.currentTimeMillis() - before
            val currentInterval = minInterval
            val sleep = currentInterval - interval
            if (sleep > 0) delay(sleep)
        }
        before = System.currentTimeMillis()
    }

}