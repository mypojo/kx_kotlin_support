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
import net.kotlinx.system.ResourceHolder
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
     * 편의용 파일 디렉토리 지정
     * @see outputStreamFactory
     * */
    var fileDir: File = ResourceHolder.WORKSPACE.slash(this::class.simpleName!!)

    /**
     * 편의용 압축 설정
     * 보통 사용자 결과인경우는 write할때는 비압축 -> 모두 모두 모아서 하나로 압축
     * s3 등으로 스플릿 할때는 write 하면서 압축
     * @see outputStreamFactory
     * */
    var gzip: Boolean = false

    /**
     * 편의용 아웃풋 스트림 제공자 (위의 두 설정은 이것을 위한것임)
     * 압축 등을 하고싶은경우 커스텀 할것
     * */
    var outputStreamFactory: (Int) -> OutputStream = { fileDir.slash("${it.padStart(3)}.csv").toOutputResource(gzip).outputStream }

    /** 분할 수. 디폴트로 엑셀 최대 크기 */
    var counter = EventCountChecker(1000000)

    /** 인코딩 등 변경에 사요 */
    var writerFactory: () -> CsvWriter = { csvWriter() }

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
            writerFactory().openAndGetRawWriter(outputStream)
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

