package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvFileWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.FlowCollector
import net.kotlinx.core.Kdsl
import net.kotlinx.io.output.OutputResource
import java.lang.AutoCloseable


/**
 * 일반적인 파일 라이트 콜렉터
 *  */
class CsvCollector : FlowCollector<List<List<String>>>, AutoCloseable {

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

