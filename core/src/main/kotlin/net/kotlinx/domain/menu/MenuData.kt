package net.kotlinx.domain.menu

/**
 * 메뉴를 Json 매핑하기 위한 데이터 클래스
 * UI에 전달되기때문에 Role 등의 데이터는 있으면 안됨
 *  */
data class MenuData(
    val id: String,
    val name: String,
    val path: String,
    val isLeaf: Boolean,
    var children: List<MenuData>
)