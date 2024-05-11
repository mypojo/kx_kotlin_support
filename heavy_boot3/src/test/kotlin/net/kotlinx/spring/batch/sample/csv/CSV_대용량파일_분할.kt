package net.kotlinx.spring.batch.sample.csv

import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.spring.batch.BatchExecutor
import net.kotlinx.spring.batch.component.CsvItemWriterTemplate
import net.kotlinx.spring.batch.component.toItemReader
import net.kotlinx.system.ResourceHolder

class CSV_대용량파일_분할 : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("BatchExecutor") {
            val limitSize = 300
            Then("파일분리 - 해당 파일을 $limitSize 건당 1개의 파일로 분리") {
                val out = ResourceHolder.getWorkspace().slash("파일분리").apply { mkdirs() }
                val context = BatchExecutor {
                    itemReader = (0..1000).toItemReader()
                    itemProcessor = { arrayOf("N${it}", "X${it}") }
                    itemWriter = CsvItemWriterTemplate {
                        header = arrayOf("일번", "이번")
                        limit = limitSize
                        this.file = out
                    }.build()
                    commitInterval = 100  //limit 는 이거의 배수가 되어야함
                }
                log.info { "결과 컨텍스트 ${context.executionContext} -> ${out.absolutePath}" }
            }
        }
    }

}