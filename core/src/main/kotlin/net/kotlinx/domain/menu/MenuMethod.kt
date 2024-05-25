package net.kotlinx.domain.menu

import java.lang.reflect.Method

/**
 * 메뉴에 달린 로직
 * 하나의 메뉴에 다수가 달릴 수 있음
 *  */
data class MenuMethod(
    var url: String,
    var clazz: Class<*>,
    var method: Method,
) {
    /** 양방향 매핑  */
    var menu: Menu? = null
}