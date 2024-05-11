package net.kotlinx.passay

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.string.toTextGrid
import org.junit.jupiter.api.Test
import org.passay.PasswordData
import org.passay.RuleResult


class PassaySetTest : BeSpecLog(){
    init {
        fun checkAndPrint(pwd: String, exp: Boolean) {
            val p = PasswordData(pwd)
            val validate: RuleResult = PassaySet.BASIC.validate(p)
            check(validate.isValid == exp)
            log.info { "패스워드 ${p.password} -> ${validate.isValid}" }
            if (validate.details.isNotEmpty()) {
                validate.details.map {
                    arrayOf(it.errorCode, it.parameters)
                }.also {
                    listOf("code", "param").toTextGrid(it).print()
                }
            }
        }

        @Test
        fun test() {
            checkAndPrint("12abc^^§§§§§§§§§", false)
            checkAndPrint("1q2w3e4r**", true)
            checkAndPrint("abcde12345", false)
            checkAndPrint("11112222!'", false)
        }
    }
}