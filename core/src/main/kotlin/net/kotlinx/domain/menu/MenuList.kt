package net.kotlinx.domain.menu


/**
 * 메뉴 저장소
 * */
open class MenuList(
    private val menus: MutableList<Menu> = mutableListOf()
) : List<Menu> by menus {

    /** 메뉴 설정 */
    fun menu(id: String, name: String, vararg roles: Enum<*>, block: Menu.() -> Unit = {}): Menu = Menu().apply(block).also { menu ->
        menu.id = id
        menu.name = name
        menu.configRoles = roles.toList()
        menus.add(menu)
    }

    /** 모든 자식들 리턴. ex) securityConfig */
    fun allChildren(): List<Menu> = this.fold<Menu, List<Menu>>(listOf()) { t, v -> t + v.allChildren() }.filter { it.isLeaf }

}