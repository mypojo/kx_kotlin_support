package net.kotlinx.string

import java.nio.charset.Charset

/** 한글 캐릭터셋 추가 */
val Charsets.MS949: Charset
    get() = Charset.forName("MS949")