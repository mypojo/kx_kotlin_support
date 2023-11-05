package net.kotlinx.core.number


const val SI_K: Long = 1024L
const val SI_M = SI_K * 1024
const val SI_G = SI_M * 1024
const val SI_T = SI_G * 1024

/** 간단한 SI 단위를 리턴해준다. 소수점 이런거 없음 */
inline fun Long.toSiText(suff: String = "byte"): String {
    return when {
        this < SI_K -> "$this $suff"
        this < SI_M -> "${this / SI_K} k$suff"
        this < SI_G -> "${this / SI_M} m$suff"
        this < SI_T -> "${this / SI_G} g$suff"
        else -> "${this / SI_T} t$suff"
    }

}