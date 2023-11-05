package net.kotlinx.spring.batch.sample.csv

import net.kotlinx.spring.batch.BatchExecutor
import net.kotlinx.spring.opencsv.CsvItemWriterTemplate
import net.kotlinx.spring.opencsv.toCsvItemWriter
import net.kotlinx.spring.opencsv.toCsvReader
import net.kotlinx.spring.resource.toGzipOutputStreamResource
import net.kotlinx.test.TestRoot
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

    @Test
    fun `인코딩변경 (UTF-8 to MS949) & 파일분리`() {
        val name = "xxx"
        doAll(File("D:\\DATA\\WORK\\temp\\$name"))
        doAll(File("D:\\DATA\\WORK\\temp\\m.${name}"))
    }

    private fun doAll(inputDir: File) {
        val outDir = File(inputDir.absolutePath + "_out").apply { mkdirs() }
        inputDir.listFiles().forEach { input ->
            val outEachDir = File(outDir, input.name).apply { mkdirs() }
            BatchExecutor {
                this.name = name
                itemReader = input.toCsvReader().utf8()
                itemWriter = CsvItemWriterTemplate {
                    limit = 500000
                    this.file = outEachDir
                }.build()
            }.also {
                log.info { "결과 컨텍스트 ${it.executionContext}" }
            }
        }
    }


}