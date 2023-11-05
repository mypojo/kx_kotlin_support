package net.kotlinx.core.menu

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

/** 설정 확장 - 아이콘 추가 */
var Menu.icon: String
    get() = this.extConfig["icon"] ?: ""
    set(value) {
        this.extConfig["icon"] = value
    }

internal class MenuTest : TestRoot() {

    enum class Role { A, B, C }

    val menus = MenuList().apply {
        menu("index", "인덱스")
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
        MENUS = this
    }

    companion object {
        lateinit var MENUS: MenuList
        lateinit var LOGIN: Menu
        lateinit var BUY2: Menu
    }

    @Test
    fun `기본테스트`() {

        val menus = MENUS.allChildren().map { v ->
            arrayOf(v.path, v.trees.joinToString(" -> ") { it.name }, v.icon)
        }.also {
            listOf("paht", "name", "icon").toTextGrid(it).print()
        }
    }
}
