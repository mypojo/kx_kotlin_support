package net.kotlinx.validation

import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test


class ValidationResultTest : TestRoot() {


    @Test
    fun test() {

        ValidationUtil.validateResult(ValidationString01().apply {
            groupName01 = "그룹1"
            groupName02 = "그룹2"
            comNo1 = "12345678"
            comNo2 = "1234567890"
            contents1 = "짧은본문글자"
            bidCost = 110
            lastInviteDate = "20230633"
            //tel = "110-1111-2222"
        }).print()


    }

}