package net.kotlinx.reflect

import net.kotlinx.concurrent.parallelExecute
import java.util.concurrent.Callable

/** CSV 등에서 간단 변환 */
inline fun <reified T : Any> List<List<String>>.fromLines(): List<T> =
    this.map { Callable { Bean.fromLine(T::class, it) } }.parallelExecute(Runtime.getRuntime().availableProcessors())