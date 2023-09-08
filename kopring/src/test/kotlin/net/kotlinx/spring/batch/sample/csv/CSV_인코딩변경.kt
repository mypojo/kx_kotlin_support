package net.kotlinx.spring.batch.sample.csv

import net.kotlinx.core.test.TestRoot
import net.kotlinx.spring.batch.BatchExecutor
import net.kotlinx.spring.opencsv.toCsvItemWriter
import net.kotlinx.spring.opencsv.toCsvReader
import net.kotlinx.spring.resource.toGzipOutputStreamResource
import org.junit.jupiter.api.Test
import java.io.File

class CSV_인코딩변경 : TestRoot() {

    @Test
    fun `csv생성&압축`() {

        val input = File("C:\\Users\\dev\\Downloads\\대표키워드\\대표키워드.csv")
        val out = File("C:\\Users\\dev\\Downloads\\대표키워드\\xxx.csv.gz")

        BatchExecutor {
            this.name = name
            itemReader = input.toCsvReader()
            itemWriter = out.toGzipOutputStreamResource().toCsvItemWriter().utf8()
        }.also {
            log.info { "결과 컨텍스트 ${it.executionContext}" }
        }

    }


}