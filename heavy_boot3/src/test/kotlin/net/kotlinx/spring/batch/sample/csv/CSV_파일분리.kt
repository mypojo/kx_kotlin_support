package net.kotlinx.spring.batch.sample.csv

import net.kotlinx.core.file.slash
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.spring.batch.BatchExecutor
import net.kotlinx.spring.batch.component.toItemReader
import net.kotlinx.spring.opencsv.CsvItemWriterTemplate
import org.junit.jupiter.api.Test

class CSV_파일분리 : BeSpecLog(){
    init {
        @Test
        fun `파일분리`() {
            val out = ResourceHolder.getWorkspace().slash("파일분리").apply { mkdirs() }
            BatchExecutor {
                itemReader = (0..1000).toItemReader()
                itemProcessor = { arrayOf("N${it}", "X${it}") }
                itemWriter = CsvItemWriterTemplate {
                    header = arrayOf("일번", "이번")
                    limit = 300
                    this.file = out
                }.build()
                commitInterval = 100  //limit 는 이거의 배수가 되어야함
            }.also {
                log.info { "결과 컨텍스트 ${it.executionContext} -> ${out.absolutePath}" }
            }
        }
    }
}