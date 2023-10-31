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

inline fun <reified T : Enum<T>> enumValueOf(str: String?, nullValue: T): T = when (str) {
    null -> nullValue
    else -> enumValueOf<T>(str)
}