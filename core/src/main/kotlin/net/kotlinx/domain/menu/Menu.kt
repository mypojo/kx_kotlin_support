package net.kotlinx.domain.menu

import net.kotlinx.core.Kdsl
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.domain.tree.Treeable

/**
 * 뎁스를 가지는 메뉴 구조
 * 각 프로젝트별로 커스터마이징(확장프로퍼티)해서 사용
 * */
class Menu : Treeable<String, Menu> {

    /** 기본 생성 */
    @Kdsl
    constructor(block: Menu.() -> Unit = {}) {
        apply(block)
    }

    /** 메뉴 ID */
    override lateinit var id: String

    /** 메뉴 명  */
    lateinit var name: String

    /** 메뉴 설명. 길어질 수 있음 */
    var descs: List<String> = emptyList()

    /** 권한 (하나라도 매핑되면 허용) */
    var configRoles: List<Enum<*>> = emptyList()

    /** 담당자 */
    var authors: List<DeveloperData> = emptyList()

    /**
     * 메뉴 속성
     * 커스텀 해서 사용하세요
     * */
    var attributes: MutableMap<String, Any> = mutableMapOf()

    /** 부모 메뉴  */
    override var parent: Menu? = null

    /** 이 구현체에서는 사용안함 */
    override var parentId: String = parent?.id ?: ""

    /** 자식 메뉴들 */
    override var children: List<Menu> = emptyList()

    /** 자식메뉴 설정 DSL. 여기 설정된 메뉴는 자식으로 입력된다 */
    fun child(id: String, name: String, vararg roles: Enum<*>, block: Menu.() -> Unit = {}): Menu = Menu(block).also { child ->
        child.id = id
        child.name = name
        if (roles.isNotEmpty()) {
            //설정된게 있을때만 오버라이드 한다.
            check(child.configRoles.isEmpty()) { "[$id] configRoles을 둘다 설정하면 안됩니다." }
            child.configRoles = roles.toList()
        }
        child.parent = this  //호출자를 부모로 할당
        this.children += child
    }

    /** 트리 전체 */
    val trees: List<Menu> by lazy { findTrees() }

    /** URL 호출경로. /로 시작하게 강제 수정 */
    val path: String by lazy { "/" + trees.joinToString("/") { it.id } }

    /** 부모 제목을 포함한 전체 이름 */
    fun title(separator: String = "->") = trees.joinToString(separator) { it.name }

    /** UI로 보여질 메뉴인지?  */
    var show = true

    /**
     * 매핑된 메소드들
     * ex) 스프링 컨트롤러 메소드
     *  */
    var menuMethods: List<MenuMethod> = emptyList()

}