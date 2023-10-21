package net.kotlinx.reflect

import net.kotlinx.aws.athena.AthenaUtil
import org.junit.jupiter.api.Test

internal class AthenaUtilTest {

    @Test
    fun `스키마출력`() {
        println(AthenaUtil.toSchema(AutobidRankLog::class).map { "${it.key} ${it.value}" }.joinToString(",\n"))
    }

}