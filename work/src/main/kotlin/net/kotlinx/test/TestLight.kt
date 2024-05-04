package net.kotlinx.test

import mu.KotlinLogging
import org.koin.core.component.KoinComponent

/**
 * 기본테스트 + 코인
 */
abstract class TestLight : KoinComponent {

    companion object {

        val log = KotlinLogging.logger {}

        init {
            MyLightKoinStarter.startup() //기본 스타트업. 오버라이드시 companion object 에 추가
        }
    }
}