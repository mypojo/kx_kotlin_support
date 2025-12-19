package net.kotlinx.aws.cognito

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.RandomStringUtil
import net.kotlinx.string.StringHpUtil
import java.util.*
import kotlin.random.Random

class CognitoComponent_시나리오 : BeSpecLight() {

    val comp by lazy {
        //demo
        CognitoComponent("ap-northeast-2_PeLL1FobS", "7lqsshvhpstcbsgdgt5hg0as9i").apply {
            this.aws = aws49
        }
    }

    val compSession by lazy {
        //demo
        CognitoSessionComponent("ap-northeast-2_PeLL1FobS", "7lqsshvhpstcbsgdgt5hg0as9i").apply {
            this.aws = aws49
        }
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("사용자 생성 / 수정 / 삭제 시나리오") {

            val username = UUID.randomUUID().toString()
            val pwd = RandomStringUtil.generateRandomPassword()
            val pwd2 = RandomStringUtil.generateRandomPassword()
            val hp = StringHpUtil.toE164Kr("010-1111-3333")
            val email = "seunghan.shin${Random.nextInt(100)}@ad.com"

        }
    }
}
