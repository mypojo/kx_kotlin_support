package net.kotlinx.kotest

import java.time.LocalDateTime

data class TestPoo01(
    var name: String? = null,
    var age: Int? = null,
    var cnt: Long? = null,
    var time: LocalDateTime? = null,
    var parent: TestPoo01? = null,
)