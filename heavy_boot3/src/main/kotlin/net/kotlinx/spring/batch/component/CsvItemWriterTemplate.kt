package net.kotlinx.spring.batch.component

import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.spring.resource.toGzipOutputStreamResource
import org.springframework.batch.item.ItemWriter
import java.io.File

/**
 * 자주 사용되는 CsvItemWriter 템플릿
 * 옵션을 조절해야 할경우 MultiResourceItemWriterBuilder 등을 직접 사용할것
 * @see net.kotlinx.file.FileZipTemplate 이거하고 같이 사용
 */
class CsvItemWriterTemplate {

    @Kdsl
    constructor(block: CsvItemWriterTemplate.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================
    /**
     * !!!주의!!! 편의상 이걸로 구분한다
     * 디렉토리인경우 (사전에 디렉토리 생성) -> MS949 & 100만건이 넘으면 분할 or 용량 크면 압축
     * 파일  -> UTF8 & gzip 압축
     * */
    lateinit var file: File

    var limit: Int = MultiResourceItemWriterBuilder.XLS_LIMIT

    var header: Array<String>? = null

    private val log = KotlinLogging.logger {}

    /** 빌드 */
    fun build(): ItemWriter<Array<String>> {
        return when {

            //MS949 -> 사람이 읽는용. 파일이 N개 까지 생길 수 있음 -> FileZipModule 로 압축할것
            file.isDirectory -> {
                if (file.listFiles().isNotEmpty()) {
                    log.warn { "Directory is not empty!!" }
                }
                MultiResourceItemWriterBuilder {
                    this.itemWriter = CsvItemWriter().apply {
                        this.header = this@CsvItemWriterTemplate.header
                    }
                    this.workspace = file
                    this.name = workspace.name
                    this.limit = this@CsvItemWriterTemplate.limit
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