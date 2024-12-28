package net.kotlinx.number


const val SI_K: Long = 1024L
const val SI_M = SI_K * 1024
const val SI_G = SI_M * 1024
const val SI_T = SI_G * 1024

/** 간단한 SI 단위를 리턴해준다. 소수점 이런거 없음 */
fun Long.toSiText(suff: String = "byte"): String {
    val org = this
    return when {
        org < SI_K -> "$this $suff"
        org < SI_M -> "${this / SI_K} k$suff"
        org < SI_G -> "${this / SI_M} m$suff"
        org < SI_T -> "${this / SI_G} g$suff"
        else -> "${this / SI_T} t$suff"
    }

}

