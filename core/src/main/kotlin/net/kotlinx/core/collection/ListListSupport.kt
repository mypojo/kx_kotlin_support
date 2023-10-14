package net.kotlinx.core.collection

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