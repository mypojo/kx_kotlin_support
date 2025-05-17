package net.kotlinx.domain.menu


/**
 * 주어진 조건을 필터링해서, MenuData를 리턴해줌
 * ex) 특정 권한으로 UI에 표시될 메뉴 리턴
 * */
fun Menu.toMenuDatas(predicate: (Menu) -> Boolean = { it.show }): List<MenuData> {
    val validSet = this.allChildren().filter { it.isLeaf }.filter(predicate).flatMap { it.trees }.toSet()
    return toMenuDatas(validSet)
}

private fun Menu.toMenuDatas(validSet: Set<Menu>): List<MenuData> {
    return this.children.filter { it in validSet }
        .map {
            MenuData(
                it.id,
                it.name,
                it.path,
                it.isLeaf,
                it.toMenuDatas(validSet)
            )
        }
}

