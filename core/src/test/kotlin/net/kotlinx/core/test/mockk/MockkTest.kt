package net.kotlinx.core.test.mockk

import io.mockk.every
import io.mockk.mockk
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

data class User(
    val id: Long,
    val type: Type,
    val name: String? = "default",
    val address: Address,
    val phones: List<String>,
    val active: Boolean
)

data class Address(
    val zipCode: String,
    val basicAddress: String,
    val detailAddress: String
)

enum class Type {
    ADMIN,
    USER
}

class UserService(
    val name: String
) {

    fun getUser(): User? {
        return User(
            id = 1,
            type = Type.ADMIN,
            name = "이름",
            address = Address(
                zipCode = "12345",
                basicAddress = "기본 주소",
                detailAddress = "상세 주소"
            ),
            phones = listOf(
                "01012345678",
                "01098765432"
            ),
            active = true
        )
    }

    fun updateUser() {}
}

/**
 *  Mockk 테스트 샘플
 */
class MockkTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("Mockk") {
            Then("Mockk-") {
                val userService = mockk<UserService>(
                    relaxed = true,  //기본 구현 없어도 오류 안남
                    relaxUnitFun = true, // relaxed는 리턴 값에 상관없이 전부 적용되는 반면, relaxUnitFun은 리턴 값이 Unit인(리턴 값이 Unit인 fun은 스터빙 하지 않아도 호출 시 에러 발생 시키지 않음) 메소드에만 적용된다.
                ) {
                    every { getUser() } returns null //재정의 가능
                }

                println(userService.name) //빈값이 자동으로 채워짐
                println(userService.getUser())
            }
        }
    }

}