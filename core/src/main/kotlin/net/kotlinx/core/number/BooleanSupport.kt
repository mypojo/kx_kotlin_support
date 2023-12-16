package net.kotlinx.core.number

/** 인라인 if 대체기 */
inline fun Boolean.ifTrue(block: Boolean.() -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}

/** 인라인 if 대체기 */
inline fun Boolean.ifFalse(block: Boolean?.() -> Unit): Boolean {
    if (!this) {
        block()
    }
    return this
}