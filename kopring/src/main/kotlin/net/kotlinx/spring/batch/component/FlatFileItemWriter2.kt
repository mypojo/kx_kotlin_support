package net.kotlinx.spring.batch.component

import org.springframework.batch.item.file.FlatFileItemWriter

/**
 * null이나 공백일경우 스킵하는 라이터
 * 기본 FlatFileItemWriter 에는 write 스킵이 없다.
 */
class FlatFileItemWriter2<T> : FlatFileItemWriter<T>() {

    init {
        setEncoding("UTF-8")
    }

    override fun doWrite(items: List<T>): String {
        val lines = StringBuilder()
        for (item in items) {
            val aggregate = lineAggregator.aggregate(item)
            if (aggregate.isNullOrEmpty()) continue
            lines.append(aggregate).append(lineSeparator)
        }
        return lines.toString()
    }

}