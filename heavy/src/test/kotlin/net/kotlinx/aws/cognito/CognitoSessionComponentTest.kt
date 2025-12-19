package net.kotlinx.aws.cognito

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class CognitoSessionComponentTest : BeSpecLight() {

    val comp by lazy {
        //demo
        CognitoSessionComponent("ap-northeast-2_PeLL1FobS", "7lqsshvhpstcbsgdgt5hg0as9i").apply {
            this.aws = aws49
        }
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("CognitoSessionComponent") {
            When("기본 시나리오") {

                Then("로그인-pass") {

                }

            }
        }
    }
}
