package net.kotlinx.csv.chunkTools

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.client.ICsvFileWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import mu.KotlinLogging
import net.kotlinx.concurrent.SuspendRunnable
import net.kotlinx.core.Kdsl
import net.kotlinx.exception.KnownException
import net.kotlinx.exception.toSimpleString
import net.kotlinx.io.input.InputResource
import net.kotlinx.io.output.OutputResource


/**
 * 대용량 CSV 읽고 쓰는 복잡한 처리기
 * 스프링 배치 의존성을 제거하는것이 핵심 (무거움) => 대신 분할쓰기 등의 기능은 없음 (어차피 뉴 메타는 분산 처리라 분할쓰기 필요없음)
 * ex) x테라의 대용량 파일 -> 100mb의 X개 파일로 분리 -> 각 분리파일을 람다에서 CsvReadWriteTools 로 읽어서 처리
 *
 * 클라우드 환경에서 쓰이는거만 남김
 * 중요!! 코루틴을 지원하며 싱글 스래드로 작동함!! -> 람다에서 최소 비용 & 싱글스래드로 청크단위로 합쳐서 코루틴 처리할것!
 *
 * 내부적으로 순서를 유지함 -> 대신 청크 안에서 병렬 처리를 할것
 * 중단된 파일 이어쓰는 기능을 하려고 했으나.. 압축이나 헤더 문제 때문에 힘듬 => 그냥 분리 & 청크처리 하는게 훨씬 좋음!
 *
 *
 * 주의!!! 이건 올드 스타일이다. 가능하면 flow를 사용하는게 고급기능 사용하기에 좋다
 *  ex) 멀티파일read, 멀티파일write, 순서없는 병렬처리 등등..
 * @see List<InputResource>.toFlow
 *  */
class CsvReadWriteTools : SuspendRunnable {

    @Kdsl
    constructor(block: CsvReadWriteTools.() -> Unit = {}) {
        apply(block)
    }

    override suspend fun run() {
        try {
            readerFactory().openAsync(inputResource.inputStream) {
                when (outputResource) {
                    null -> doProcess(null)
                    else -> writerFactory().openAsync(outputResource!!.outputStream) {
                        try {
                            doProcess(this)
                        } catch (e: KnownException.ItemSkipException) {
                            log.warn { "현재 item을 skip 합니다! -> ${e.toSimpleString()}" }
                        }
                    }
                }
            }
        } catch (e: KnownException.StopException) {
            log.warn { "작업을 중단합니다! -> ${e.toSimpleString()}" }
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
        var index = 0
        while (iterator.hasNext()) {
            chunkDatas.add(iterator.next())
            if (chunkDatas.size == chunkSize) {
                processor.invoke(CsvChunk(index++, chunkDatas.toList(), header, csvWriter))
                csvWriter?.flush() //중단시 재시도를 위해서
                chunkDatas.clear()
            }
        }

        // 남은 데이터 처리
        if (chunkDatas.isNotEmpty()) {
            processor.invoke(CsvChunk(index++, chunkDatas.toList(), header, csvWriter))
            csvWriter?.flush()
        }
    }

    //==================================================== 처리 설정 ======================================================

    /** 라인단위 처리 */
    lateinit var processor: suspend (CsvChunk) -> Unit

    /** 청크 크기 */
    var chunkSize: Int = 100

    /** 첫 로우가 헤더인지? */
    var withHeader: Boolean = false

    //==================================================== IN (필수) ======================================================

    /**
     * reader factory
     * 인코딩이나 delimiter 등을 조절
     * @see net.kotlinx.csv.CsvUtil
     *  */
    var readerFactory: () -> CsvReader = { csvReader() }

    /** 입력 */
    lateinit var inputResource: InputResource

    //==================================================== OUT (선택) ======================================================

    /**
     * writer 팩토리
     * ex) writerFactory = { CsvUtil.ms949Writer() }
     *  */
    var writerFactory: () -> CsvWriter = { csvWriter() }

    /** 출력 */
    var outputResource: OutputResource? = null


    companion object {
        private val log = KotlinLogging.logger {}
    }

}

