package net.kotlinx.csv.chunkTools

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.client.ICsvFileWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.concurrent.SuspendRunnable
import net.kotlinx.core.Kdsl
import net.kotlinx.exception.KnownException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPOutputStream


/**
 * 대용량 CSV 읽고 쓰는 복잡한 처리기
 * 스프링 배치 의존성을 제거하는것이 핵심 (무거움) => 대신 분할쓰기 등의 기능은 없음 (어차피 뉴 메타는 분산 처리라 분할쓰기 필요없음)
 *
 * 클라우드 환경에서 쓰이는거만 남김
 * 중요!! 코루틴을 지원하며 싱글 스래드로 작동함!! -> 람다에서 최소 비용 & 싱글스래드로 청크단위로 합쳐서 코루틴 처리할것!
 *  */
class CsvReadWriteTools : SuspendRunnable {

    @Kdsl
    constructor(block: CsvReadWriteTools.() -> Unit = {}) {
        apply(block)
    }

    override suspend fun run() {
        readerFactory().openAsync(readerInputStream) {
            when (writerOutputStream) {
                null -> doProcess(null)
                else -> writerFactory().openAsync(writerOutputStream!!) { doProcess(this) }
            }
        }
    }

    private suspend fun CsvFileReader.doProcess(csvWriter: ICsvFileWriter?) {
        val iterator = readAllAsSequence().iterator()

        if (!iterator.hasNext()) throw KnownException.ItemSkipException("비어있는 CSV입니다")

        // 첫 번째 라인을 헤더로 처리
        val header = if (withHeader) iterator.next() else null
        processor.invoke(CsvChunk(-1, emptyList(), header, csvWriter)) //스타트 콜백은 -1로 넘김

        // 청크 단위로 데이터 처리
        val chunkDatas = mutableListOf<List<String>>()
        var index: Int = 0
        while (iterator.hasNext()) {
            chunkDatas.add(iterator.next())
            if (chunkDatas.size == chunkSize) {
                processor.invoke(CsvChunk(index++, chunkDatas.toList(), header, csvWriter))
                chunkDatas.clear()
            }
        }

        // 남은 데이터 처리
        if (chunkDatas.isNotEmpty()) {
            processor.invoke(CsvChunk(index++, chunkDatas.toList(), header, csvWriter))
        }
    }

    //==================================================== 처리 설정 ======================================================

    /** 라인단위 처리 */
    lateinit var processor: suspend (CsvChunk) -> Unit

    /** 청크 크기 */
    var chunkSize: Int = 100

    /** 첫 로우가 헤더인지? */
    var withHeader: Boolean = false

    /** 첫 로우가 헤더인지? */
    var before: Boolean = false

    //==================================================== IN (필수) ======================================================

    /**
     * reader factory
     * 인코딩이나 delimiter 등을 조절
     * @see net.kotlinx.csv.CsvUtil
     *  */
    var readerFactory: () -> CsvReader = { csvReader() }

    /** 입력 스트림 */
    lateinit var readerInputStream: InputStream

    /** 입력 파일 -> 스트림 */
    var readerFile: File? = null
        set(value) {
            readerInputStream = value!!.inputStream()
            field = value
        }

    //==================================================== OUT (선택) ======================================================

    /**
     * writer 팩토리
     * ex) writerFactory = { CsvUtil.ms949Writer() }
     *  */
    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** 출력 스트림 */
    val writerOutputStream: OutputStream?
        get() = when {

            writerFile != null -> {
                if (writerGzip) GZIPOutputStream(FileOutputStream(writerFile)) else FileOutputStream(writerFile)
            }

            else -> null
        }

    /** 파일로 쓸 경우 압축 여부 */
    var writerGzip: Boolean = false

    /** 결과파일 -> 스트림 */
    var writerFile: File? = null


}

