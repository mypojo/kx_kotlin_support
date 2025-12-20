package net.kotlinx.collection

/**
 * 모든 경우의 수를 전부 리턴. 다양한 응용이 가능함.
 * ex) "ABCFG".toList().map { listOf(it.toString(),"") }.buildMatrix().map { it.toSet() }.distinct().filter { it.size > 1 }.map { it.joinToString("") }
 *   => [ABCFG, BCFG, ACFG, CFG, ABFG, BFG, AFG, FG, ABCG, BCG, ACG, CG, ABG, BG, AG, G, ABCF, BCF, ACF, CF, ABF, BF, AF, F, ABC, BC, AC, C, AB, B, A]
 * ex) listOf(listOf(a, "-"), listOf(b, "-"), listOf(c, "-"), listOf(d, "-")).buildMatrix().map { it.joinToString("") }
 *  */
fun <T> List<List<T>>.buildMatrix(): List<List<T>> {
    return this.fold(listOf()) { sum, data: List<T> ->
        if (sum.isEmpty()) return@fold data.map { listOf(it) } //toList 안써도 되는듯?
        data.map { v -> sum.map { it + v } }.reduce { a, b -> a + b }
    }
}

/**
 * List<List<String>>의 모든 내부 리스트를 가장 큰 크기에 맞춰서 빈 문자열로 채운다.
 * Google Sheet에서 읽은 데이터처럼 각 행의 크기가 다를 때 유용하다.
 */
fun List<List<String>>.normalize(defaultValue: String = ""): List<List<String>> {
    if (this.isEmpty()) return this
    val maxSize = this.maxOfOrNull { it.size } ?: 0
    return this.map { row ->
        if (row.size < maxSize) {
            row + List(maxSize - row.size) { defaultValue }
        } else {
            row
        }
    }
}