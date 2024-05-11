package net.kotlinx.domain.tree

/** 인메모리 트리구조 vo를 간단히 연결해주는 도구  */
object TreeUtil {

    /**
     * 부모ID만 알고있을경우 트리 구조로 만들어준다.
     * @return root list
     */
    fun <ID, T : Treeable<ID, T>> buildWithParentId(list: Collection<T>): List<T> {
        if (list.isEmpty()) return emptyList()

        //부모 지정
        list.associateBy { it.id }.let { byId ->
            list.forEach {
                it.parentId ?: return@forEach

                it.parent = byId[it.parentId]
                checkNotNull(it.parent) { "부모키가 존재하지만 부모객체를 찾을 수 없습니다. ${it.parentId}" }
            }
        }
        //자식 지정
        list.filter { it.parentId != null }.groupBy { it.parentId }.let { byParent ->
            list.forEach { it.children = byParent[it.id] ?: emptyList() }
        }
        return list.filter { it.parent == null }
    }


}