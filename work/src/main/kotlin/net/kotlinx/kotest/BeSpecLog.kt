package net.kotlinx.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestScope
import mu.KotlinLogging

/**
 * 간단 스펙 재정의
 * 로거 추가
 * */
abstract class BeSpecLog : BehaviorSpec() {

    /**
     * 공용 로거.
     * 이거 하나로 같이 써도, UI에서 로그가 잘 보이기때문에 큰 문제 없음
     * */
    val log = KotlinLogging.logger {}

    init {
        KotestLogUtil.logLevelInit()
    }


    /** 테스트 이름 간단출력 */
    protected fun TestScope.printName() {
        log.info { "============ ${this.testCase.name.testName} ============" }
    }
}