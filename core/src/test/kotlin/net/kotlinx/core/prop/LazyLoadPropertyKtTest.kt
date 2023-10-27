package net.kotlinx.core.prop

import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.time.TimeString
import org.junit.jupiter.api.Test

class LazyLoadPropertyKtTest : TestRoot() {

    val demo: String by lazyLoad {
        log.info { "초기화됩니다!!" }
        "xxx"
    }

    val ts: TimeString by lazyLoad {
        log.info { "TimeString 초기화됩니다!!" }
        TimeString(123123)
    }

    @Test
    fun `데모`() {
        println(demo)
        println(demo)
        lzayLoadReset(String::class.java)
        println(demo)

        println(ts)
        println(ts)
        lzayLoadReset(TimeString::class.java)
        println(ts)
    }

}