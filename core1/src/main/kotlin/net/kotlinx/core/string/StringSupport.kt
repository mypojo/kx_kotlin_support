package net.kotlinx.core.string

/**
 * 자주 사용되는건데 null 버전이 없어서 만들었음
 * @see ifEmpty
 * @see ifBlank
 *  */
inline fun CharSequence?.ifNullOrEmpty(block: () -> String): String {
    return if (this.isNullOrEmpty()) {
        block()
    } else {
        this.toString()
    }
}