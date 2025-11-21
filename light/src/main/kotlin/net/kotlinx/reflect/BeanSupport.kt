package net.kotlinx.reflect

import net.kotlinx.concurrent.parallelExecute
import java.util.concurrent.Callable

/** CSV 등에서 간단 변환 */
inline fun <reified T : Any> List<List<String>>.fromLines(): List<T> =
    this.map { Callable { Bean.fromLine(T::class, it) } }.parallelExecute(Runtime.getRuntime().availableProcessors())

/** CSV 등에서 간단 변환 (유연 매핑 버전) */
inline fun <reified T : Any> List<List<String>>.fromLinesIgnore(): List<T> =
    this.map { Callable { Bean.fromLineIgnore(T::class, it) } }.parallelExecute(Runtime.getRuntime().availableProcessors())