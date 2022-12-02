package net.kotlinx.kotlinSupport.string

/** replace 밖에 없어서 만들었음 */
inline fun String.retain(regex:Regex):String = regex.findAll(this).joinToString("") { it.value }