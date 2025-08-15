package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvFileWriter
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.FlowCollector
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.file.slash
import net.kotlinx.io.output.toOutputResource
import java.io.File
import java.io.OutputStream
import java.lang.AutoCloseable

/**
 * 각 라인의 separator 함수 결과로 파일을 분할하여 CSV를 쓰기하는 콜렉터
 * 기존 CsvSplitCollector와 달리 라인 수가 아닌 특정 필드 값으로 파일을 분할함
 */
class CsvNamedSplitCollector : FlowCollector<List<List<String>>>, AutoCloseable {

    @Kdsl
    constructor(block: CsvNamedSplitCollector.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정파일 ======================================================

    /**
     * 각 라인에서 파일명을 결정하는 함수
     * 반환값이 파일명으로 사용됨
     */
    lateinit var separator: (List<String>) -> String

    /**
     * 편의용 아웃풋 스트림 제공자
     * 파일명을 받아서 OutputStream을 생성함
     */
    lateinit var outputStreamFactory: (String) -> OutputStream

    /**
     * 간단 내부 outputStreamFactory 생성기
     * @param gzip 압축 여부
     */
    fun File.toOutputStreamFactory(gzip: Boolean = false): (String) -> OutputStream {
        return { fileName -> 
            val extension = if (gzip) ".csv.gz" else ".csv"
            this.slash("$fileName$extension").toOutputResource(gzip).outputStream 
        }
    }

    /** 인코딩 등 변경에 사용 */
    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** CSV 파일의 헤더. null이면 헤더를 쓰지 않음 */
    var headers: List<String>? = null

    //==================================================== 내부사용 ======================================================

    /** 파일명별 CsvFileWriter 캐시 */
    private val writerCache = mutableMapOf<String, CsvFileWriter>()

    override suspend fun emit(lines: List<List<String>>) {
        lines.forEach { line ->
            val fileName = separator(line)
            val writer = writerCache.getOrPut(fileName) {
                log.trace { "새 파일 writer 생성: $fileName" }
                val outputStream = outputStreamFactory(fileName)
                val newWriter = writerFactory().openAndGetRawWriter(outputStream)
                
                // 새 파일이 생성될 때 헤더가 설정되어 있으면 헤더를 먼저 씀
                headers?.let { headerList -> newWriter.writeRow(headerList) }
                
                newWriter
            }
            writer.writeRow(line)
        }
    }

    override fun close() {
        writerCache.values.forEach { it.close() }
        writerCache.clear()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}