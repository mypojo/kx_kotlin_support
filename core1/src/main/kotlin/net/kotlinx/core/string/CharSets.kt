package net.kotlinx.core.string

import java.nio.charset.Charset

/** 자주사용되는 캐릭터셋 모음 */
object CharSets {

    /** 기본 */
    val UTF_8: Charset = Charsets.UTF_8

    /** 한글 */
    val MS949: Charset = Charset.forName("MS949")

}