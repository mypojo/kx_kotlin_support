package net.kotlinx.kotest

import io.mockk.every
import io.mockk.mockkStatic
import net.kotlinx.string.capital
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDateTime

/**
 *  Mockk 테스트 샘플
 */
class MockkEtcTest : BeSpecLog() {

    companion object {
        fun helloWorld(): String {
            return "hello world"
        }
    }

    init {
        initTest(KotestUtil.FAST)

        Given("기본 모킹 테스트") {
            Then("자바 static 모킹") {
                mockkStatic(LocalDateTime::class)

                every { LocalDateTime.now() } returns LocalDateTime.of(2023, 5, 3, 0, 0, 0)

                assertEquals(LocalDateTime.of(2023, 5, 3, 0, 0, 0), LocalDateTime.now())
            }

            fun String.textCapitalizeWords(): String {
                val words = this.split(" ")
                val capitalizedWords = words.map { it.capital() }
                return capitalizedWords.joinToString(" ")
            }

            //확인할것
//            Then("탑레벨 모킹") {
//                mockkStatic(::helloWorld)
//
//                every { helloWorld() } returns "yeah"
//
//                println(helloWorld())
//                assertEquals("yeah", helloWorld())
//
//                unmockkStatic(::helloWorld)
//
//                println(helloWorld())
//            }
//
//            Then("확장함수 모킹") {
//                mockkStatic(String::textCapitalizeWords)
//
//                every { "test".textCapitalizeWords() } returns "test"
//
//                assertEquals("test", "test".textCapitalizeWords())
//
//                every { helloWorld() } returns "yeah"
//
//                assertEquals("yeah", helloWorld())
//            }
        }
    }

}