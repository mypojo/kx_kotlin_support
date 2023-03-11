package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.guardduty.CfnDetector
import software.amazon.awscdk.services.guardduty.CfnDetectorProps


object GuardDutyUtil {

    /** 5인규모 플젝 기준 매일 2$ 정도 나옴 */
    fun guardDutyOn(stack: Stack, name: String) {
        CfnDetector(stack, "$name-guardDuty", CfnDetectorProps.builder().enable(true).build())
    }


}