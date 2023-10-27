package net.kotlinx.core.test.mockk

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

fun helloWorld(): String {
    return "hello world"
}

fun String.textCapitalizeWords(): String {
    val words = this.split(" ")
    val capitalizedWords = words.map { it.capitalize() }
    return capitalizedWords.joinToString(" ")
}

/**
 *  Mockk 테스트 샘플
 */
class MockkEtcTest : TestRoot() {


    @Test
    fun `자바 static 모킹`() {
        mockkStatic(LocalDateTime::class)

        every { LocalDateTime.now() } returns LocalDateTime.of(2023, 5, 3, 0, 0, 0)

        assertEquals(LocalDateTime.of(2023, 5, 3, 0, 0, 0), LocalDateTime.now())
    }

    @Test
    fun `탑레벨 모킹`() {
        mockkStatic(::helloWorld)

        every { helloWorld() } returns "yeah"

        println(helloWorld())
        assertEquals("yeah", helloWorld())

        unmockkStatic(::helloWorld)

        println(helloWorld())
    }

    @Test
    fun `확장함수 모킹`() {
        mockkStatic(String::textCapitalizeWords)

        every { "test".textCapitalizeWords() } returns "test"

        assertEquals("test", "test".textCapitalizeWords())

        every { helloWorld() } returns "yeah"

        assertEquals("yeah", helloWorld())
    }

}