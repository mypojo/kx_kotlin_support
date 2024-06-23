package net.kotlinx.number

/**
 * 인라인 if 대체기
 * true이면 block 호출후 결과리턴
 * false 이면 null이기 때문에 ?: 가능
 *  */
inline fun <T> Boolean.ifTrue(block: Boolean.() -> T): T? {
    return if (this) {
        block()
    } else {
        null
    }
}

/**
 * 인라인 if 대체기
 * false이면 block 호출후 결과리턴
 *  */
inline fun <T> Boolean.ifFalse(block: Boolean?.() -> T): T? {
    return if (!this) {
        block()
    } else {
        null
    }
}