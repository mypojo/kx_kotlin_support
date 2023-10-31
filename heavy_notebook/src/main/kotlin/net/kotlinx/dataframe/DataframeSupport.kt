package net.kotlinx.dataframe

import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf

/**
 * 간단 변환용
 * toDataFrame 써도 됨
 *  */
fun List<List<String>>.toAnyFrame(): AnyFrame = dataFrameOf(this[0], this.drop(1).flatten())