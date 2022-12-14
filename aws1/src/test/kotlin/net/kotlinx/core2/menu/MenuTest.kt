package net.kotlinx.core2.menu

import net.kotlinx.aws1.TestRoot
import net.kotlinx.core1.string.toTextGrid
import org.junit.jupiter.api.Test

/** 설정 확장 - 아이콘 추가 */
var Menu.icon: String
    get() = this.extConfig["icon"] ?: ""
    set(value) {
        this.extConfig["icon"] = value
    }

internal class MenuTest : TestRoot() {

    enum class Role { A, B, C }

    class MyMenuList : MenuList() {

        init {
            menu("market", "마켓") {
                child("make01", "메뉴A", Role.A, Role.B) {
                    child("buy1", "최종구매1", Role.C) { icon = "market.gif" }
                    child("buy2", "최종구매2", Role.C).also { BUY2 = this }
                }
                child("make02", "메뉴B", Role.A, Role.C) { authors = listOf("aa", "bb") }
            }
            menu("member", "회원") {
                child("login", "로그인", Role.A, Role.B).apply { LOGIN = this; icon = "login.gif" }
                child("logout", "로그아웃", Role.A, Role.C)
            }
        }

        companion object {
            val MENUS = MyMenuList()
            lateinit var LOGIN: Menu
            lateinit var BUY2: Menu
        }

    }

    @Test
    fun `기본테스트`() {

        val menus = MyMenuList.MENUS.fold(listOf<Menu>()) { t, v -> t + v.allChildren() }
        menus.filter { it.isLeaf }.map { v ->
            arrayOf(v.path, v.trees.joinToString(" -> ") { it.name }, v.icon)
        }.also {
            listOf("paht", "name", "icon").toTextGrid(it).print()
        }
    }
}
