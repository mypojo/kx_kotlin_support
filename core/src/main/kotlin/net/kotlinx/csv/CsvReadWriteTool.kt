package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.core.Kdsl
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.GZIPOutputStream


/**
 * 파일을 즉시 읽고 다시 써주는 간이도구
 * 람다에서 800mb 가량의 데이터 처리 시도시 에러난다.. 디스크 문제일수도 있음
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

    /** 라인단위 처리 */
    var processor: (List<String>) -> List<String> = { it }

    //==================================================== IN ======================================================

    /** 입력파일 */
    var readerFile: File? = null
        set(value) {
            readerInputStream = value!!.inputStream()
            field = value
        }

    /**
     * reader factory
     * 인코딩이나 delimiter 등을 조절
     *  */
    var readerFactory: () -> CsvReader = { csvReader() }

    /** 간단 캐릭터셋 변경 */
    fun readerCharset(value: Charset) {
        readerFactory = { csvReader { charset = value.name() } }
    }

    /** 입력파일 스트림 */
    lateinit var readerInputStream: InputStream

    //==================================================== OUT ======================================================

    /** 결과파일 */
    lateinit var writerFile: File

    /** writer 팩토리 */
    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** 간단 캐릭터셋 변경 */
    fun writerCharset(value: Charset) {
        writerFactory = { csvWriter { charset = value.name() } }
    }

    /** 압축 여부 */
    var writerGzip: Boolean = false


}

