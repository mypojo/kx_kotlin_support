package net.kotlinx.test

import org.koin.core.component.KoinComponent

/**
 * 기본테스트 + 코인
 */
abstract class TestLight : TestRoot(), KoinComponent {

    companion object {
        init {
            MyLightKoinStarter.startup() //기본 스타트업. 오버라이드시 companion object 에 추가
        }
    }
}