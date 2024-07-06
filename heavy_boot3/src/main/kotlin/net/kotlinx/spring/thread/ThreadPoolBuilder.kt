package net.kotlinx.spring.thread

import mu.KotlinLogging
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.util.ErrorHandler
import java.util.concurrent.TimeUnit

/**
 * ThreadPoolTaskExecutor 가 아닌 ThreadPoolTaskScheduler를 사용한다.  <-- 차이점은 정교한 풀링을 못한다는거.. (근데 대부분 필요없더라)
 *
 * 2가지 용도가 있다.
 *
 * 1. 쿼츠 안쓰는 간단 스케쥴러
 * ex) scheduleAtFixedRate로 할경우 3초 주기인데 3초안에 안끝날경우, 중복실행되지는 않고 종료 즉시 재시작한다.
 *
 * 2. 스래드풀 : 1개의 작업을 N개의 스래드로 돌리는게 아니라 N개의 작업에 N개의 스래드를 할당해서 동시에 작업할때사용
 * ex) 라이센스 단위로 작업을 분산해아햐는 네이버
 * ex) 파일을 읽고 처리해서 쓰는 작업시 순서를 보장해야하는 경우
 *
 * 스래드에 예외가 발생한다면 해당 스래드는 즉시 종료된다. 나머지 스래드는 그냥 진행. (당연하잔아..)
 *
 * 예외 처리 타입
 * 1. 일반 형태 : 예외가 나면 개만 처리하고 무시.  ThreadExecutor를 싱글톤으로 유지.
 * 2. 스프링 배치형 : 전체 스래드풀에 있는 모든  스래드를 인터럽트함.(즉 한개 스래드에서 에러나면 전부 다시 실행해야함)  ThreadExecutor를 매번 생성해서 사용
 *
 * 참고 ) https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/concurrent/ThreadPoolTaskExecutor.html
 *
 * ============================================ 파라메터 설명 ======================================================
 * activeCount : 현재 라이브된 스래드 수.
 * corePoolSize : 최초 설정. 이걸 넘어가면 큐에 쌓임. 요즘 큐는 사실상 무한대로 설정.
 * == 이하 무시해도 될듯 ==
 * maxPoolSize : 큐가 다 차면 이게 늘어나는데.. 의미없다.
 * keepAliveSeconds : 이내로 들어오는 작업에 대해서는 스래드를 재사용 한다.
 * poolSize : 현재 풀에 저장된 스래드. 사실상 corePoolSize랑 다를게 없음
 *
 *
 * 작동원리는  DelayQueue.
 *
 * 참고
 * -> 스래드를 많이 생성하더라도 누적이 안되고 킬 된다면 수백만개 정도는 별 문제 없음
 * -> 과거 일자로 스캐쥴링을 추가하면 즉시 실행됨
 *
 */
class ThreadPoolBuilder(
    /** 풀 사이즈 (스래드 수) */
    val poolSize: Int,
    /** threadNamePrefix 를 추가해서 , 여기에서 생성된 스래드임을 짐작하게 할 수 있다.  */
    val name: String = ThreadPoolBuilder::class.simpleName!!,
    /**
     * 셧다운시 모든 스래드에 인터럽트를 내릴지 여부.
     * 일반 풀 형태는 true -> 셧다운시 모든 스래드의 종료를 대기
     * 스프링 배치 형태는 false ->  셧다운시 모든 스래드를  인터럽트함.
     * */
    val waitForTasksToCompleteOnShutdown: Boolean = true,
    /**
     *  shutdown시 전체 스래드의 종료를 얼마나 기다릴지. 지정된 시간 내에서 메인 스래드가 블락된다. 넘으면 걍 진행
     *  중요! 타임아웃이 끝나도 메인 스래드만 반환되는거임. 기존 스래드는 인터럽트 되지 않고 그냥 쭉 진행되는중
     *  */
    val awaitTerminationSeconds: Int = TimeUnit.HOURS.toSeconds(24).toInt(),
) {

    private val log = KotlinLogging.logger {}

    /** 스래드 올리기 전에 에러 핸들러 지정해야 한다. 에러 핸들러를 들고 스래드가 올라가는듯. 나중에 바꿔봐야 적용 안됨 */
    var errorHandler: ErrorHandler = ErrorHandler { e ->
        throw e
    }

    fun build(block: ThreadPoolTaskScheduler.() -> Unit = {}): ThreadPoolTaskScheduler = ThreadPoolTaskScheduler().apply {
        this.poolSize = poolSize
        setWaitForTasksToCompleteOnShutdown(true) //
        setAwaitTerminationSeconds(awaitTerminationSeconds)
        setErrorHandler(errorHandler)
        setThreadGroupName("$name-")
        setThreadNamePrefix(name)
        isDaemon = true //혹시나 메모리 문제로 추가..
        block(this)
        afterPropertiesSet()
    }

}
