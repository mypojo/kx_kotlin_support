package net.kotlinx.passay

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGridPrint
import org.passay.PasswordData
import org.passay.RuleResult


class PassaySetTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("PassaySet") {

            fun checkAndPrint(pwd: String, exp: Boolean) {
                val p = PasswordData(pwd)
                val validate: RuleResult = PassaySet.BASIC.validate(p)
                check(validate.isValid == exp)
                log.info { "패스워드 ${p.password} -> ${validate.isValid}" }
                listOf("실패 code", "실패 param").toTextGridPrint {
                    validate.details.map {
                        arrayOf(it.errorCode, it.parameters)
                    }
                }
            }

            Then("비밀번호 검증") {
                checkAndPrint("12abc^^§§§§§§§§§", false)
                checkAndPrint("1q2w3e4r**", true)
                checkAndPrint("abcde12345", false)
                checkAndPrint("11112222!'", false)
            }
        }
    }
}