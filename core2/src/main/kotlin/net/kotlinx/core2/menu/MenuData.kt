package net.kotlinx.core2.menu

import java.lang.reflect.Method

data class MenuData(
    var url: String,
    var clazz: Class<*>,
    var method: Method,
) {
    /** 양방향 매핑  */
    var menu: Menu? = null
}