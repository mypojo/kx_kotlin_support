package net.kotlinx.core.string

//==================================================== fun이 많아지더라도 regex 보다 string에 있는게 더 자연스럽다. ======================================================

/**
 * 해당 매칭만 남기고 제거한다.
 *  */
inline fun String.retainFrom(regex: Regex): String = regex.findAll(this).joinToString("") { it.value }

/**
 * 해당 매칭을 제거한다.
 *  */
inline fun String.removeFrom(regex: Regex): String = this.replace(regex, "")