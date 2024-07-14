package net.kotlinx.domain.menu

import io.kotest.matchers.shouldBe
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGridPrint


internal class MenuTest : BeSpecLog() {

    private enum class Role { A, B, C }

    companion object {

        /** 설정 확장 - 아이콘 추가 */
        private var Menu.icon: String
            get() = this.attributes["icon"]?.toString() ?: ""
            set(value) {
                this.attributes["icon"] = value
            }

        val MENUS: MenuList = MenuList {
            menu("index", "인덱스")
            menu("market", "마켓") {
                child("make01", "메뉴A") {
                    child("buy1", "최종구매1") {
                        configRoles = setOf(Role.A, Role.B)
                        icon = "market.gif"
                    }
                    child("buy2", "최종구매2") {
                        configRoles = setOf(Role.A, Role.C)
                        BUY2 = this // companion 할당
                    }
                }
                child("make02", "메뉴B") {
                    authors = setOf(DeveloperData("sin"))
                }
            }
            menu("member", "회원") {
                child("login", "로그인") {
                    LOGIN = this
                    icon = "login.gif"
                }
                child("logout", "로그아웃")
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

            Then("companion 할당 확인") {
                LOGIN.icon shouldBe "login.gif"
                BUY2.configRoles shouldBe listOf(Role.A, Role.C)
            }
        }
    }


}
