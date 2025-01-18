package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.core.Kdsl
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.GZIPOutputStream


/**
 * 대용량 CSV 읽고 쓰는 간단 처리기
 * 스프링 배치 의존성을 제거하는것이 핵심 (무거움)
 * 람다에서 800mb 가량의 데이터 처리 시도시 에러남. 디스크 확보 필요
 *
 * 간단한데만 사용할것
 * @see net.kotlinx.csv.chunkTools.CsvReadWriteTools
 *  */
class CsvReadWriteTool {

    @Kdsl
    constructor(block: CsvReadWriteTool.() -> Unit = {}) {
        apply(block)

        val reader = readerFactory()
        val writer = writerFactory()

        reader.open(readerInputStream) {
            val stream = if (writerGzip) GZIPOutputStream(FileOutputStream(writerFile)) else FileOutputStream(writerFile)
            writer.open(stream) {
                readAllAsSequence().forEach { line ->
                    writeRow(processor(line))
                }
            }
        }
    }

    //==================================================== 처리 설정 ======================================================

    /** 라인단위 처리 */
    var processor: (List<String>) -> List<String> = { it }

    //==================================================== IN ======================================================

    /** 입력 스트림 */
    lateinit var readerInputStream: InputStream

    /** 입력 파일 -> 스트림 */
    var readerFile: File? = null
        set(value) {
            readerInputStream = value!!.inputStream()
            field = value
        }

    /**
     * reader factory
     * 인코딩이나 delimiter 등을 조절
     * @see CsvUtil
     *  */
    var readerFactory: () -> CsvReader = { csvReader() }

    //==================================================== OUT ======================================================

    /** 결과파일 */
    lateinit var writerFile: File

    /**
     * writer 팩토리
     * ex) writerFactory = { CsvUtil.ms949Writer() }
     *  */
    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** 압축 여부 */
    var writerGzip: Boolean = false


}

