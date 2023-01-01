package net.kotlinx.module1.reflect

import org.junit.jupiter.api.Test

internal class AthenaReflectionUtilTest {

    @Test
    fun `스키마출력`() {
        println(AthenaReflectionUtil.toSchema(AutobidRankLog::class).map { "${it.key} ${it.value}" }.joinToString(",\n"))
    }

}