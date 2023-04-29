package net.kotlinx.kopring.opencsv

import mu.KotlinLogging
import net.kotlinx.kopring.spring.batch.component.MultiResourceItemWriterBuilder
import net.kotlinx.kopring.spring.toGzipOutputStreamResource
import org.springframework.batch.item.ItemWriter
import java.io.File

/**
 * 자주 사용되는 CsvItemWriter 템플릿
 */
class CsvItemWriterTemplate(
    val block: CsvItemWriterTemplate.() -> Unit
) {

    //==================================================== 설정 ======================================================
    /**
     * 파일은경우  -> gzip 압축
     * 디렉토리인경우 -> 100만건이 넘으면 분할
     * */
    lateinit var file: File

    var header: Array<String>? = null

    private val log = KotlinLogging.logger {}

    /**
     * 빌드
     * @see net.kotlinx.core2.file.FileZipTemplate
     * */
    fun build(): ItemWriter<Array<String>> {
        block(this)
        return when {

            //MS949 -> 사람이 읽는용. 파일이 N개 까지 생길 수 있음 -> FileZipModule 로 압축할것
            file.isDirectory -> {
                if (file.listFiles().isNotEmpty()) {
                    log.warn("Directory is not empty!!")
                }
                MultiResourceItemWriterBuilder {
                    this.itemWriter = CsvItemWriter().apply {
                        this.header = this@CsvItemWriterTemplate.header
                    }
                    this.workspace = file
                    this.name = workspace.name
                }.build()
            }

            //UTF-8  -> 시스템이 읽는용. 파일 1개
            else -> {
                CsvItemWriter().apply {
                    this.header = this@CsvItemWriterTemplate.header
                    this.utf8()
                    this.setResource(file.toGzipOutputStreamResource())
                }
            }

        }


    }


}