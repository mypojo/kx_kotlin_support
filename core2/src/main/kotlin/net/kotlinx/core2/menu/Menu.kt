package net.kotlinx.core2.menu

import net.kotlinx.core1.tree.Treeable

/**
 * 뎁스를 가지는 메뉴 구조
 * 각 프로젝트별로 커스터마이징(확장프로퍼티)해서 사용
 * */
data class Menu(
    /** 메뉴 ID */
    override var id: String = "",
    /** 메뉴 명  */
    var name: String = "",
) : Treeable<String, Menu> {

    /** 권한 (하나라도 매핑되면 허용) */
    var configRoles: List<out Enum<*>> = emptyList()

    /** 담당자 */
    var authors: List<String> = emptyList()

    /** 부모 메뉴  */
    override var parent: Menu? = null

    /** 이 구현체에서는 사용안함 */
    override var parentId: String = parent?.id ?: ""

    /** 자식 메뉴들 */
    override var children: List<Menu> = emptyList()

    /** 자식메뉴 설정 DSL. 여기 설정된 메뉴는 자식으로 입력된다 */
    fun child(id: String, name: String, vararg roles: Enum<*>, block: Menu.() -> Unit = {}): Menu = Menu().apply(block).also { child ->
        child.id = id
        child.name = name
        child.configRoles = roles.toList()
        this.children = children + child
        child.parent = this
    }

    /** 트리 전체 */
    val trees: List<Menu> by lazy { findTrees() }

    /** URL 호출경로. /로 시작하게 강제 수정 */
    val path: String by lazy { "/" + trees.joinToString("/") { it.id } }

    /** UI로 보여질 메뉴인지?  */
    var show = true

    /** 확장 프로퍼티를 위한 저장소 */
    val extConfig: MutableMap<String, String> = mutableMapOf()

    /** 매핑 */
    var menuDatas: List<MenuData> = emptyList()

}