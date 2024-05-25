package net.kotlinx.domain.menu

import net.kotlinx.core.Kdsl


/**
 * 메뉴 저장소
 * */
class MenuList {

    @Kdsl
    constructor(block: MenuList.() -> Unit = {}) {
        apply(block)
    }

    /** 루트 메뉴  (1뎁스)  */
    private val _roots: MutableList<Menu> = mutableListOf()

    val roots: List<Menu>
        get() = _roots

    /** 메뉴 설정 */
    fun menu(id: String, name: String, vararg roles: Enum<*>, block: Menu.() -> Unit = {}): Menu = Menu(block).also { menu ->
        menu.id = id
        menu.name = name
        if (roles.isNotEmpty()) {
            //설정된게 있을때만 오버라이드 한다.
            check(menu.configRoles.isEmpty()) { "[$id] configRoles을 둘다 설정하면 안됩니다." }
            menu.configRoles = roles.toList()
        }
        _roots.add(menu)
    }

    /** 모든 자식들 리턴. ex) securityConfig */
    fun allChildren(): List<Menu> = _roots.fold<Menu, List<Menu>>(listOf()) { t, v -> t + v.allChildren() }.filter { it.isLeaf }

}