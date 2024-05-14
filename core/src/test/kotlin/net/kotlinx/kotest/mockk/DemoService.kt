package net.kotlinx.kotest.mockk

import io.mockk.every
import io.mockk.mockk

class DemoService(
    val demoRepository: DemoRepository
) {
    /** 나이를 하나 올린다. */
    fun updateUser(id: String): User {
        val user = demoRepository.findById(id)
        user.age++
        return user
    }
}

/** 데이터베이스에서 사용자를 가져오는 저장소 */
class DemoRepository {
    fun findById(id: String): User {
        throw IllegalStateException("$id DB 접속이 너무 오래걸려요")
    }
}

/** Entity class */
class User {
    var id: Long = 0
    var name: String = "default"
    var age: Long = 0
}

fun kockDemoRepository(): DemoRepository = mockk<DemoRepository>(
    relaxed = true,  //기본 구현 없어도 오류 안남
    relaxUnitFun = true, // relaxed는 리턴 값에 상관없이 전부 적용되는 반면, relaxUnitFun은 리턴 값이 Unit인(리턴 값이 Unit인 fun은 스터빙 하지 않아도 호출 시 에러 발생 시키지 않음) 메소드에만 적용된다.
) {
    every { findById("123") } returns User().apply {
        id = 123
        name = "mock"
        age = 10
    }
}