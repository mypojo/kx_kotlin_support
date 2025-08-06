package net.kotlinx.spring.thread

import net.kotlinx.time.toInstant
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Callable
import java.util.concurrent.Future
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration


/** 셧다운 하고 초기화 한다.  */
fun ThreadPoolTaskScheduler.shutdownAndInit() {
    this.shutdown()
    this.initialize() //이거 해야 다시 태스크를 받을 수 있다.
}

/**
 * 전부 실행 후 future를 반환
 * corePoolSize 가 아니고 poolSize 를 참조한다
 *  */
fun <O> ThreadPoolTaskScheduler.executeAll(run: () -> O?): List<Future<O?>> = (0 until corePoolSize).map { this.submit(Callable { run() }) }

/** 전부 대기 */
fun <O> List<Future<O?>>.wait(): List<O?> = this.map { it.get() }

//=========================== 간단 메소드 ===============================
/** 대기중인 큐 사이즈 가져옴  */
val ThreadPoolTaskScheduler.queueSize: Int
    get() = scheduledThreadPoolExecutor.queue.size

/** 설정된 최대 스래드.즉 최초에 입력한 값 . 더 좋은방법이 있는지 모르겠다.  */
val ThreadPoolTaskScheduler.corePoolSize: Int
    get() = scheduledThreadPoolExecutor.corePoolSize

/** 최대 스래드 - 현재 작동중인 스래드 = 사용 가능한 스래드  */
val ThreadPoolTaskScheduler.remainPoolSize: Int
    get() = corePoolSize - activeCount

//==================================================== 스케쥴 실행 ======================================================

/**
 * 특정 시간 기준으로 하루 한번 작동 샘플
 * ex) 하루에 한번 작동하는 배치
 *  */
fun ThreadPoolTaskScheduler.scheduleAtFixedDate(hh: Int, mm: Int, durations: Duration = 1.days, runnable: Runnable) {
    val localTime = LocalTime.of(hh, mm)!!
    val localDateTime = LocalDateTime.now().with(localTime)
    this.scheduleAtFixedRate(runnable, localDateTime.toInstant(), durations.toJavaDuration())
}