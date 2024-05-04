package net.kotlinx.aws1

import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test

internal class AwsInstanceTypeUtilTest : BeSpecLog(){
    init {
        @Test
        fun `기본테스트`() {
            println(AwsInstanceTypeUtil.INSTANCE_TYPE)
        }

    }
}