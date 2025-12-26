package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvFileWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.FlowCollector
import net.kotlinx.core.Kdsl
import net.kotlinx.io.output.OutputFileResource
import net.kotlinx.io.output.OutputResource
import java.io.File
import java.lang.AutoCloseable


/**
 * 일반적인 파일 라이트 콜렉터
 *  */
class CsvCollector : FlowCollector<List<List<String>>>, AutoCloseable {


    companion object {

        /** 인라인 간단 생성자  */
        fun utf8(file: File, header: List<String>? = null, block: CsvCollector.() -> Unit = {}): CsvCollector = CsvCollector {
            outputResource = OutputFileResource(file)
            this.header = header
            block()
        }

        /** 인라인 간단 생성자 (MS949)  */
        fun ms949(file: File, header: List<String>? = null, block: CsvCollector.() -> Unit = {}): CsvCollector = CsvCollector {
            outputResource = OutputFileResource(file)
            this.header = header
            writer = CsvUtil.ms949Writer()
            block()
        }
    }

    @Kdsl
    constructor(block: CsvCollector.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정파일 ======================================================

    /** 파일 제공자 */
    lateinit var outputResource: OutputResource

    /** 헤더 */
    var header: List<String>? = null

    /** 쓰기 객체 */
    var writer: CsvWriter = csvWriter()

    //==================================================== 내부사용 ======================================================

    var rawWriter: CsvFileWriter? = null

    override suspend fun emit(lines: List<List<String>>) {
        if (rawWriter == null) {
            rawWriter = writer.openAndGetRawWriter(outputResource.outputStream)
            header?.let { rawWriter!!.writeRow(it) }
        }
        lines.forEach { rawWriter!!.writeRow(it) }
    }

    override fun close() {
        rawWriter?.close()
    }

}

