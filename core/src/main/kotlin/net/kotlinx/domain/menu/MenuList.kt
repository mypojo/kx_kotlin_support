package net.kotlinx.domain.menu

import mu.KotlinLogging
import net.kotlinx.collection.Trie
import net.kotlinx.core.Kdsl
import java.lang.reflect.Method


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
            menu.configRoles = roles.toSet()
        }
        _roots.add(menu)
    }

    /** 모든 자식들 리턴. ex) securityConfig */
    fun allChildren(): List<Menu> = _roots.fold<Menu, List<Menu>>(listOf()) { t, v -> t + v.allChildren() }.filter { it.isLeaf }

    /**
     * 스프링 컨트롤러 정보를 가져와서 매핑 -> 스프링에서 제공하지 않는 편의 유틸
     * 메뉴의 path (짧은 URL) 를 사용해서 실제 매핑 URL(긴 URL)을 매핑해준다.
     *  */
    fun regisg(methods: Collection<MenuMethod>) {
        allChildren().forEach { eachMenu ->
            val methods = methods.filter { it.url.startsWith(eachMenu.path) }
            methods.onEach { it.menu = eachMenu }
            eachMenu.menuMethods = methods
            log.trace { "메뉴 [${eachMenu.path}] -> 메뉴 메소드 ${methods.size}건 매핑 (${methods.joinToString(",") { it.method.name }})" }
        }
    }

    /** 스프링 메소드로부터 매핑 정보 조회 */
    private val menuMethods: Map<Method, MenuMethod> by lazy { allChildren().flatMap { it.menuMethods }.associateBy { it.method } }

    /** 스프링 메소드로부터 매핑 정보 조회 */
    operator fun get(method: Method): MenuMethod? = menuMethods[method]


    //==================================================== 메뉴 조회 (특수목적용) ======================================================

    /** 메뉴맵.  path 는 중복되면 안됨 */
    val menuMap: Map<String, Menu> by lazy { allChildren().associateBy { it.path } }

    /** 접두어 검색기 */
    private val menuTrie: Trie by lazy { Trie(menuMap.keys) }

    /** 접두어로 메뉴를 찾아줌 */
    fun findMenu(prefix: String): Menu? {
        val prefixes = menuTrie.findPrefixes(prefix)
        check(prefixes.size <= 1)

        if (prefixes.isEmpty()) return null

        return menuMap[prefixes.first()]
    }

    //==================================================== 메뉴메소드 조회 (특수목적용) ======================================================

    /** 메뉴메소드  url 는 중복되면 안됨 */
    val menuMethodMap: Map<String, MenuMethod> by lazy { allChildren().flatMap { it.menuMethods }.associateBy { it.url } }

    /** 접두어 검색기 */
    private val menuMethodTrie: Trie by lazy { Trie(menuMethodMap.keys) }

    /**
     * 접두어로 메뉴메소드를 찾아줌
     * 만약 path 변수 등을 사용해서 정확한 매칭이 어려운경우 그냥 findMenu 로 구조를 수정할것!
     *  */
    fun findMenuMethod(prefix: String): MenuMethod? {
        val prefixes = menuMethodTrie.findPrefixes(prefix)
        check(prefixes.size <= 1)

        if (prefixes.isEmpty()) return null

        return menuMethodMap[prefixes.first()]
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}