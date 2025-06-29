package net.kotlinx.validation.bean

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy


class HibernateValidationTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("기본 폼 벨리데이션") {
            When("오류 메세지 체크") {
                val demo = ValidationDemoString().apply {
                    groupName = "너무긴 그룹네임 테스트"
                    userType = "New"
                    userTypeName = "123abc"
                    ip = "aa123.888.999.55598"
                    hp = "전화번호"
                    vchar = "영감님"
                    lastInviteDate = "20240332"
                    bidCost = 113
                }
                val results = HibernateValidation.validate(demo)
                results.printSimple() //그리드 프린팅

                Then("디폴트 매핑이 사용됨") {
                    results.first { it.fieldId == "name" }.message shouldBe "이름 : 필수입력항목입니다"
                }
                Then("커스텀 매핑이 사용됨") {
                    results.first { it.fieldId == "name2" }.message shouldBe "이름을 입력해주세요"
                }
                Then("커스텀 매핑에 attr가  사용됨") {
                    results.first { it.fieldId == "groupName" }.message shouldBe "그룹명 :  문자열 길이가 4에서 8 사이여야 합니다"
                }
            }

            When("정상 체크") {
                val demo = ValidationDemoString().apply {
                    name = "a"
                    name2 = "b"
                    ip = "123.123.123.111"
                    vchar = "영감"
                    lastInviteDate = "20240330"
                    bidCost = 110
                }
                val results = HibernateValidation.validate(demo)
                results.printSimple()
                Then("모든 값 정상통과") {
                    results.size shouldBe 0
                }
            }
        }
    }


}
