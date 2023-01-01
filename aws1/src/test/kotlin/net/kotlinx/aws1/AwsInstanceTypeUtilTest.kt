package net.kotlinx.aws1

import org.junit.jupiter.api.Test

internal class AwsInstanceTypeUtilTest : TestRoot() {
    @Test
    fun `기본테스트`() {
        println(AwsInstanceTypeUtil.instanceType)
    }

}