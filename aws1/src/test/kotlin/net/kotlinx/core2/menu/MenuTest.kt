package net.kotlinx.core2.menu

import net.kotlinx.aws1.TestRoot
import org.junit.jupiter.api.Test

internal class MenuTest : TestRoot() {

    enum class Role {
        A, B, C
    }

    class MyMenuList : MenuList() {

        init {
            menu("market", "마켓") {
                child("make01", "메뉴A", Role.A, Role.B) {
                    child("buy1", "최종구매1", Role.C)
                    child("buy2", "최종구매2", Role.C).also { BUY2 = it }
                }
                child("make02", "메뉴B", Role.A, Role.C) { authors = listOf("aa", "bb") }
            }
            menu("member", "회원") {
                child("login", "로그인", Role.A, Role.B).also { LOGIN = it }
                child("logout", "로그아웃", Role.A, Role.C)
            }
        }

        companion object {
            lateinit var LOGIN: Menu
            lateinit var BUY2: Menu
        }


    }

    @Test
    fun `기본테스트`() {

        MyMenuList().forEach { println(it) }

        println(MyMenuList.BUY2)
        println(MyMenuList.BUY2.root)

    }
}
