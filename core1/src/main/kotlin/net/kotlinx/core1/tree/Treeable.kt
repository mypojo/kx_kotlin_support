package net.kotlinx.core1.tree

import net.kotlinx.core1.Identity


/** 부모와 자식을 가지는 트리 구조를 표현한다.   */
interface Treeable<ID, T : Treeable<ID, T>> : Identity<ID> {

    /** 자신 ID */
    override val id: ID

    /** 부모 ID (DB내용 등으로 연결관계를 만들때 사용됨)  */
    val parentId: ID?

    /** 부모.  */
    var parent: T?

    /** 자식 */
    var children: List<T>

    //==================================================== 구현 ======================================================

    /** 마지막 노드인지? */
    val isLeaf: Boolean
        get() = children.isEmpty()

    /** root 인지?  중요!! null이면 root 로 간주한다. */
    val isRoot: Boolean
        get() = parent == null

    /** 루트를 리턴한다.  */
    val root: T
        get() = if (isRoot) this as T else this.parent!!.root

    /** leaf 기준으로 root -> 최상위 노드 까지 순서대로 리스팅  */
    fun findTrees(): List<T> {
        val datas: MutableList<T> = mutableListOf()
        var parent = this
        while (true) {
            datas.add(0, parent as T)
            if (parent.parent == null) break
            parent = parent.parent as T
        }
        return datas.toList()
    }

    private fun allChildren(menus: MutableList<T>) {
        menus.add(this as T)
        children.forEach { it.allChildren(menus) }
    }

    /**
     * 하위 메뉴들을 전부 모아준다.
     * ex) val menus = MyMenuList().fold(listOf<Menu>()) { t, v -> t + v.allChildren() }
     * */
    fun allChildren(): List<T> {
        val menus = mutableListOf<T>()
        allChildren(menus)
        return menus.toList()
    }


}