package net.kotlinx.core1.tree

/** 부모와 자식을 가지는 트리 구조를 표현한다.   */
interface Treeable<ID, T : Treeable<ID, T>> {

    /** 자신 ID */
    val id: ID

    /** 부모 ID */
    val parentId: ID?

    /** 부모 */
    var parent: T?

    /** 자식 */
    var children: List<T>

    //==================================================== 구현 ======================================================

    /** 마지막 노드인지 체크  */
    val isLeaf: Boolean

    /** 루트를 리턴한다.  */
    val root: T
        get() = when (this.parent) {
            null -> this as T
            else -> this.parent!!.root
        }

//    /** root -> 최상위 노드 까지 순서대로 리스팅  */
//    val trees:List<T>  by lazy {
//        val datas: List<T> = mutableListOf()
//        var parent = this
//        while (true) {
//            datas.add(0, parent)
//            if (parent.parent == null) break
//            parent = parent.parent
//        }
//        return datas
//    }


//
//    /** 뎁스를 리턴한다. 루트 = 0뎁스이다.  */
//    fun <ID : Serializable?, T : Treeable<ID, T>?> findDepth(tree: T): Int {
//        var depth = 0
//        var parent: T? = tree
//        while (true) {
//            parent = parent.parent
//            if (parent == null) return depth
//            depth++
//            check(depth <= 100) { "root is not found from $tree" }
//        }
//    }
//


}