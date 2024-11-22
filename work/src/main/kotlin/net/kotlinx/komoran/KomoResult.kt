package net.kotlinx.komoran

data class KomoResult(
    val kwdName: String,
    val pos: String,
    /** 형태소 코드 한글명 */
    val posName: String,
    val cnt: Int,
)