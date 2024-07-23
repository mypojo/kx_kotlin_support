package net.kotlinx.domain.menu

import java.lang.reflect.Method

/**
 * 메뉴에 달린 로직
 * 하나의 메뉴에 다수가 달릴 수 있음
 *  */
data class MenuMethod(
    /** 매핑 URL */
    var url: String,
    /** 컨트롤러 클래스 */
    var clazz: Class<*>,
    /** 매핑 매소드. 여기서 각 설명 같은거 읽어올것! */
    var method: Method,
) {
    /** 양방향 매핑  */
    var menu: Menu? = null

    /** 커스텀 속성 */
    val attributes: MutableMap<String, Any> = mutableMapOf()
}