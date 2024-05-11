package net.kotlinx.test

import mu.KotlinLogging
import org.koin.core.component.KoinComponent

/**
 * 기본테스트 + 코인
 */
abstract class TestLight : KoinComponent {

    companion object {

        val log = KotlinLogging.logger {}

    }
}