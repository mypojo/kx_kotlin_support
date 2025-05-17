package net.kotlinx.domain.menu

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.kotlinx.delegate.MapAttributeDelegate
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGridPrint


internal class MenuTest : BeSpecLog() {

    private enum class Role { A, B, C }

    companion object {

        /** 설정 확장  ->  이렇게 하면 지저분함  */
        private var Menu.icon: String
            get() = this.attributes["icon"]?.toString() ?: ""
            set(value) {
                this.attributes["icon"] = value
            }

        /** 설정 확장 - 위임자로 확장 */
        var Menu.title: String by MapAttributeDelegate<String>()

        /** 설정 확장 - 위임자로 확장 */
        var Menu.age: Long by MapAttributeDelegate<Long>()


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
                MARKET = this
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
        lateinit var MARKET: Menu
    }


    init {
        initTest(KotestUtil.FAST)

        Given("MenuList") {

            Then("메뉴 리스팅") {
                listOf("path", "name", "icon").toTextGridPrint {
                    MENUS.allChildren().map { v ->
                        arrayOf(v.path, v.trees.joinToString(" -> ") { it.name }, v.icon)
                    }
                }
            }

            When("메뉴 권한 to JSON") {
                Then("전체가 리턴됨"){
                    val menus = MARKET.toMenuDatas { it.show && Role.A in it.configRoles }
                    println(GsonData.fromObj(menus).toPreety())
                }
                Then("권한 있는거먄 리턴됨"){
                    val menus = MARKET.toMenuDatas { it.show && Role.C in it.configRoles }
                    println(GsonData.fromObj(menus).toPreety())
                }
            }

            Then("companion 할당 확인") {
                LOGIN.icon shouldBe "login.gif"
                BUY2.configRoles shouldBe listOf(Role.A, Role.C)
            }

            When("메뉴 확장") {

                val m = Menu()
                m.name = "test"

                Then("없는데 가져오면 예외") {
                    shouldThrow<IllegalStateException> {
                        println(m.title)
                    }
                    shouldThrow<IllegalStateException> {
                        println(m.age)
                    }
                }

                Then("입력하면 정상 출력") {
                    m.title = "하마"
                    m.title shouldBe "하마"

                    m.age = 778
                    m.age shouldBe 778
                }


            }
        }
    }


}
