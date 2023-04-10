package net.kotlinx.core2.concurrent

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable

internal class ThreadSupportTest {

    @Test
    fun `기본테스트`() {
        val execute = (0..4).map {
            Callable {
                println("wait... $it")
                Thread.sleep(it * 1000L)
                it
            }
        }.parallelExecute(4)
        println(execute)
    }

}