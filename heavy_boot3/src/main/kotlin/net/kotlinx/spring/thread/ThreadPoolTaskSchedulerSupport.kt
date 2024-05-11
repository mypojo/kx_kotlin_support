package net.kotlinx.spring.thread

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.Callable
import java.util.concurrent.Future


/** 셧다운 하고 초기화 한다.  */
fun ThreadPoolTaskScheduler.shutdownAndInit() {
    this.shutdown()
    this.initialize() //이거 해야 다시 태스크를 받을 수 있다.
}

/** 전부 실행 후 future를 반환 */
fun <O> ThreadPoolTaskScheduler.executeAll(run: () -> O?): List<Future<O?>> = (0 until corePoolSize).map { this.submit(Callable { run() }) }

/** 전부 대기 */
fun <O> List<Future<O?>>.wait(): List<O?> = this.map { it.get() }

//=========================== 간단 메소드 ===============================
val ThreadPoolTaskScheduler.queueSize: Int
    /** 대기중인 큐 사이즈 가져옴  */
    get() = scheduledThreadPoolExecutor.queue.size
val ThreadPoolTaskScheduler.corePoolSize: Int
    /** 설정된 최대 스래드.즉 최초에 입력한 값 . 더 좋은방법이 있는지 모르겠다.  */
    get() = scheduledThreadPoolExecutor.corePoolSize
val ThreadPoolTaskScheduler.remainPoolSize: Int
    /** 최대 스래드 - 현재 작동중인 스래드 = 사용 가능한 스래드  */
    get() = corePoolSize - activeCount

