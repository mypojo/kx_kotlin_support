package net.kotlinx.kopring.spring.batch.sample.csv

import net.kotlinx.core1.threadlocal.ResourceHolder
import net.kotlinx.core2.file.FileZipTemplate
import net.kotlinx.kopring.opencsv.CsvItemWriterTemplate
import net.kotlinx.kopring.spring.batch.BatchExecutor
import net.kotlinx.kopring.spring.batch.component.toItemReader
import org.junit.jupiter.api.Test
import java.io.File

class CSV_간단쓰기_테스트 : net.kotlinx.core2.test.TestRoot() {

    @Test
    fun `csv생성&압축`() {
        val workspace = File(ResourceHolder.getWorkspace(), this::class.simpleName)
        workspace.deleteRecursively() //주의!!
        workspace.mkdirs()
        doTest("utf8-10", 10, File(workspace, "utf8-10.csv.gz"))
        doTest("utf8-1300000", 1300000, File(workspace, "utf8-1300000.csv.gz"))
        doTest("ms949-10", 10, File(workspace, "ms949-10").apply { mkdirs() })
        doTest("ms949-1300000", 1300000, File(workspace, "ms949-1300000").apply { mkdirs() })
    }

    private fun doTest(name: String, cnt: Int, file: File) {
        //dir.mkdirs()
        BatchExecutor {
            this.name = name
            itemReader = (0..cnt).toItemReader()
            itemProcessor = { arrayOf("N${it}", "X${it}") }
            itemWriter = CsvItemWriterTemplate {
                header = arrayOf("일번", "이번")
                this.file = file
            }.build()
            commitInterval = 3
        }.also {
            log.info { "결과 컨텍스트 ${it.executionContext}" }
        }
        val result = FileZipTemplate(1).zip(file)
        log.info { "결과파일 -> $result" }
    }


}