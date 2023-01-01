package net.kotlinx.core2.menu

import net.kotlinx.core1.tree.Treeable

/** 메뉴 설정 DSL */
fun menu(id: String, name: String, vararg roles: Enum<*>, block: Menu.() -> Unit): Menu = Menu().apply {
    this.id = id
    this.name = name
    this.configRoles = roles.toList()
}.apply(block)

/**
 * 뎁스를 가지는 메뉴 구조
 * */
data class Menu(
    /** 메뉴 ID */
    override var id: String = "",
    /** 메뉴 명  */
    var name: String = "",
    /** 권한 (하나라도 매핑되면 허용) */
    var configRoles: List<out Enum<*>> = emptyList(),
    /** 담당자 */
    var authors: List<String> = emptyList(),
) : Treeable<String, Menu> {

    override var parentId: String = ""
    /** 부모 메뉴  */
    override var parent: Menu? = null

    override val isLeaf: Boolean
        get() = children.isEmpty()

    /** 자식 메뉴들 */
    override var children: List<Menu> = emptyList()

    /** 자식메뉴 설정 DSL */
    fun child(id: String, name: String, vararg roles: Enum<*>, block: Menu.() -> Unit = {}): Menu = Menu().apply(block).also { child ->
        child.id = id
        child.name = name
        child.configRoles = roles.toList()
        this.children = children + child
    }



//
//    /** URL 호출경로. 시작은 "/"로 한다.  */
//    private var path: String? = null
//
//    /** 각 메뉴별로 할당된 유니크한 ID  */
//    private var menuId: String? = null
//
//    @ManyToOne
//    private var parent: Menu? = null
//    protected var configRole: MutableSet<ConfigAttribute>
//    protected var hideRole: MutableSet<ConfigAttribute>
//
//    @OneToMany
//    private var children: MutableList<Menu>
//    private var isLeaf: Boolean
//
//    /** 상단 메뉴에서 보여줄지 여부  */
//    protected var show = true
//
//    /** 상단 메뉴에서 보여줄지 여부  */
//    protected var showHeaderTitie = true
//
//    /** 사이드바 메뉴에서 보여줄지 여부  */
//    protected var showSidebarMenu = true
//
//    /** 등록 메뉴 인지 여부  */
//    protected var regMenu = false
//
//    /** 모바일 여부  */
//    protected var mobileType = false
//
//    @NotExpose
//    private val mappings: List<MenuMapping> = Lists.newArrayList()
//
//    /** 일단 사용하는데 없음  */
//    private val isNew = false
//
//    /** icon no - 메뉴별로 특정 icon no(String) 이 필요해서 추가함(1뎁스만 해당됨)  */
//    private var iconNo: String? = null
//    fun setIconNo(iconNo: String?): Menu {
//        this.iconNo = iconNo
//        return this
//    }
//
//    fun hide(): Menu {
//        show = false
//        return this
//    }
//
//    fun hideHeaderTitle(): Menu {
//        showHeaderTitie = false
//        return this
//    }
//
//    fun hideSidebarMenu(): Menu {
//        showSidebarMenu = false
//        return this
//    }
//
//    fun trueRegMenu(): Menu {
//        regMenu = true
//        return this
//    }
//
//    val isMobile: Menu
//        get() {
//            mobileType = true
//            return this
//        }
//
//    // 설정한 권한을 가지고 있는 사용자한테는 메뉴가 보이지 않는다.
//    fun hideRole(vararg attributes: GrantedAuthority?): Menu {
//        for (auth in attributes) {
//            hideRole.add(SecurityUtil.authToConfig(auth))
//        }
//        return this
//    }
//
//    /**
//     * static에서 접근해도 정상적으로 나오도록 편의상 매번 호출하도록 수정함.
//     */
//    private fun init() {
//        val menuJoin = menuJoin
//        path = "/" + StringUtil.join(menuJoin, "/")
//        menuId = StringUtil.join(menuJoin, "") //HTML이나 자바스크립트의 ID로 사용되기도 하기때문에 .같은걸로 연결 못한다.
//        for (each in children) {
//            each.init()
//        }
//    }
//
//    /** 모든 부모가 정상적으로 지정되어야 한다. ID를 생성할때 한정적으로 사용  */
//    private val menuJoin: List<String>
//        private get() {
//            val menuId: List<String> = Lists.newArrayList()
//            var current: Menu? = this
//            while (current != null) {
//                menuId.add(0, current.parentId)
//                current = current.parent
//            }
//            return menuId
//        }
//
//    /**
//     * 자신을 기준으로 부모부터 담긴 리스트를 반환한다.
//     * 0번째가 루트, 마지막이 자신
//     */
//    val menuTree: List<Menu>
//        get() {
//            val menuList: List<Menu> = Lists.newArrayList()
//            var current: Menu? = this
//            while (current != null) {
//                menuList.add(0, current)
//                current = current.parent
//            }
//            return menuList
//        }
//
//    /** 간단한 메뉴 설명. 일단 이력에서 사용  */
//    val menuDescription: String
//        get() = menuTree.stream().map<Any> { v: Menu -> v.getName() }.collect(Collectors.joining(" => "))
//
//    /** HTML 메뉴 링크를 리턴한다.  */
//    val hrefLink: String
//        get() = getPath()
//
//    fun cloneThis(): Menu {
//        return try {
//            this.clone() as Menu
//        } catch (e: CloneNotSupportedException) {
//            throw RuntimeException(e)
//        }
//    }
//
//    /*
//	이거 쓰면 constructor와 처리로직이 달라서 menulist가 이상하게 만들어짐. 쓰지말자.
//	 */
//    @Deprecated("")
//    fun addChildren(child: Menu) {
//        children.add(child)
//    }
//
//    override operator fun compareTo(o: Menu): Int {
//        return menuId!!.compareTo(o.menuId!!)
//    }
//
//    override fun equals(obj: Any?): Boolean {
//        if (this === obj) return true
//        if (obj == null) return false
//        if (javaClass != obj.javaClass) return false
//        val other = obj as Menu
//        if (menuId == null) {
//            if (other.menuId != null) return false
//        } else if (menuId != other.menuId) return false
//        return true
//    }
//
//    override fun hashCode(): Int {
//        val prime = 31
//        var result = 1
//        result = prime * result + if (menuId == null) 0 else menuId.hashCode()
//        return result
//    }
//
//    companion object {
//        private const val serialVersionUID = 1L
//    }
}