package net.kotlinx.aws1

import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

internal class AwsInstanceTypeUtilTest : TestRoot() {
    @Test
    fun `기본테스트`() {
        println(AwsInstanceTypeUtil.INSTANCE_TYPE)
    }

}