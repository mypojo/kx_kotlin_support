package net.kotlinx.aws.iam

import io.kotest.matchers.ints.shouldBeGreaterThan
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGridPrint

class IamCredentialTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("IamCredential") {

            val credential = IamCredential()

            Then("프로파일 전체 정보 출력") {
                credential.profileDatas.size shouldBeGreaterThan 4
                listOf("profileName", "설명").toTextGridPrint {
                    credential.profileDatas.map { arrayOf(it.profileName, it.desc) }
                }
            }

            Then("프로파일 STS index 리스트") {
                listOf("index", "profileName").toTextGridPrint {
                    credential.profileDatas.filter { it.awsId != null }.mapIndexed { i, it -> arrayOf(i, it.profileName) }
                }
            }

        }
    }

}
