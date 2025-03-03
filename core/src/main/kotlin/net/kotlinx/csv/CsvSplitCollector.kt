package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvFileWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.FlowCollector
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.counter.EventCountChecker
import net.kotlinx.file.slash
import net.kotlinx.number.padStart
import net.kotlinx.system.ResourceHolder
import java.io.File
import java.lang.AutoCloseable


/**
 * flow 를 사용해서 CSV를 분할 쓰기 해주는 콜렉터
 * 기존의 스프링 배치 모듈을 대체함
 *  */
class CsvSplitCollector : FlowCollector<List<List<String>>>, AutoCloseable {

    @Kdsl
    constructor(block: CsvSplitCollector.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정파일 ======================================================

    /** 파일 제공자 */
    var fileFactory: (Int) -> File = { ResourceHolder.WORKSPACE.slash(this::class.simpleName!!).slash("${it.padStart(3)}.csv") }

    /** 분할 수 */
    var counter = EventCountChecker(10)

    //==================================================== 내부사용 ======================================================

    /** current 쓰기 객체 */
    var writer: CsvFileWriter? = null

    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** 일련번호 */
    var fileIndex = 0

    override suspend fun emit(lines: List<List<String>>) {
        writer = counter.callOrFirst {
            log.trace { "call index $it" }
            writer?.close()
            val file = fileFactory(fileIndex++)
            writerFactory().openAndGetRawWriter(file.outputStream())
        } ?: writer
        lines.forEach { writer!!.writeRow(it) }
    }

    override fun close() {
        writer?.close()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}

