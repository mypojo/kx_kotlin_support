package net.kotlinx.domain.menu

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGridPrint


internal class MenuTest : BeSpecLog() {

    private enum class Role { A, B, C }

    companion object {

        /** 설정 확장 - 아이콘 추가 */
        private var Menu.icon: String
            get() = this.extConfig["icon"] ?: ""
            set(value) {
                this.extConfig["icon"] = value
            }


        val MENUS: MenuList = MenuList().apply {
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
        }
        lateinit var LOGIN: Menu
        lateinit var BUY2: Menu
    }

    init {
        initTest(KotestUtil.FAST)

        Given("MenuList") {
            Then("메뉴 리스팅") {
                listOf("paht", "name", "icon").toTextGridPrint {
                    MENUS.allChildren().map { v ->
                        arrayOf(v.path, v.trees.joinToString(" -> ") { it.name }, v.icon)
                    }
                }
            }
        }
    }


}
