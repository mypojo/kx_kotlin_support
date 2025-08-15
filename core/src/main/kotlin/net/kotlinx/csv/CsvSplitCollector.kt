package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvFileWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.FlowCollector
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.counter.EventCountChecker
import net.kotlinx.file.slash
import net.kotlinx.io.output.toOutputResource
import net.kotlinx.number.padStart
import java.io.File
import java.io.OutputStream
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

    /**
     * 편의용 아웃풋 스트림 제공자 (위의 두 설정은 이것을 위한것임)
     * 압축 등을 하고싶은경우 커스텀 할것
     * */
    lateinit var outputStreamFactory: (Int) -> OutputStream

    /**
     * 간단 내부 outputStreamFactory 생성기
     * 보통 사용자 결과인경우는 write할때는 비압축 -> 모두 모두 모아서 하나로 압축
     *  s3 등으로 스플릿 할때는 write 하면서 압축
     *  @see outputStreamFactory
     * */
    fun File.toOutputStreamFactory(gzip: Boolean = false): (Int) -> OutputStream {
        return {
            val extension = if (gzip) ".csv.gz" else ".csv"
            this.slash("${it.padStart(3)}$extension").toOutputResource(gzip).outputStream
        }
    }

    /** 분할 수. 디폴트로 엑셀 최대 크기 */
    var counter = EventCountChecker(1000000)

    /** 인코딩 등 변경에 사요 */
    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** CSV 파일의 헤더. null이면 헤더를 쓰지 않음 */
    var headers: List<String>? = null

    //==================================================== 내부사용 ======================================================

    /** current 쓰기 객체 */
    var writer: CsvFileWriter? = null

    /** 일련번호 */
    var fileIndex = 0

    override suspend fun emit(lines: List<List<String>>) {
        writer = counter.callOrFirst {
            log.trace { "call index $it" }
            writer?.close()
            val outputStream = outputStreamFactory(fileIndex++)
            val newWriter = writerFactory().openAndGetRawWriter(outputStream)
            
            // 새 파일이 생성될 때 헤더가 설정되어 있으면 헤더를 먼저 씀
            headers?.let { headerList -> newWriter.writeRow(headerList) }
            
            newWriter
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
