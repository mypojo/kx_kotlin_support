package net.kotlinx.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestScope
import mu.KotlinLogging
import net.kotlinx.system.SystemSeparator

/**
 * 간단 스펙 재정의
 * 로거 추가
 *
 * ==== 설정관련 주의사항 ====
 * parallelism 올리는경우 다른 콘솔에 로그가 찍힐 수 있으니 주의!
 * */
@Suppress("LeakingThis")
abstract class BeSpecLog : BehaviorSpec() {

    val testClassName = this::class.qualifiedName!!

    /**
     * 공용 로거.
     * 이거 하나로 같이 써도, UI에서 로그가 잘 보이기때문에 큰 문제 없음
     * */
    protected val log = KotlinLogging.logger {}

    init {
        KotestLogUtil.logLevelInit()

        if (!SystemSeparator.IS_GRADLE) {
            //그래들 환경이 아니라면 테스트 중으로 간주한다.
            val log = KotlinLogging.logger {}
            log.warn { "개별 테스트 작동 -> kotest TESTING TAG 추가.." }
            tags(KotestUtil.TESTING)
        }

    }


    /** 테스트 이름 간단출력 */
    protected fun TestScope.printName() {
        log.info { "============ ${this.testCase.name.testName} ============" }
    }
}