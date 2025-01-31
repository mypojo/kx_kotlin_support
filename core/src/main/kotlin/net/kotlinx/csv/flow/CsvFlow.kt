package net.kotlinx.csv.flow

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import net.kotlinx.core.Kdsl
import net.kotlinx.csv.chunkTools.CsvChunk
import net.kotlinx.exception.KnownException
import java.io.File
import java.io.InputStream

/**
 * 대용량 CSV 읽기 flow
 *  */
class CsvFlow : Flow<CsvChunk> {

    @Kdsl
    constructor(block: CsvFlow.() -> Unit = {}) {
        apply(block)
    }

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

    /** 청크 크기 */
    var chunkSize: Int = 100

    /** 첫 로우가 헤더인지? */
    var withHeader: Boolean = false

    /**
     * 시작 콜백
     *  */
    var flowStart: (header: List<String>?) -> Unit = {}

    override suspend fun collect(collector: FlowCollector<CsvChunk>) {
        readerFactory().openAsync(readerInputStream) {

            val iterator = readAllAsSequence().iterator()

            if (!iterator.hasNext()) throw KnownException.StopException("비어있는 CSV입니다") //없어도 될듯

            // 첫 번째 라인을 헤더로 처리
            val header = if (withHeader) iterator.next() else null
            flowStart(header)

            // 청크 단위로 데이터 처리
            val chunkDatas = mutableListOf<List<String>>()
            var index: Int = 0
            while (iterator.hasNext()) {
                chunkDatas.add(iterator.next())
                if (chunkDatas.size == chunkSize) {
                    collector.emit(CsvChunk(index++, chunkDatas.toList(), header))
                    chunkDatas.clear()
                }
            }

            // 남은 데이터 처리
            if (chunkDatas.isNotEmpty()) {
                collector.emit(CsvChunk(index++, chunkDatas.toList(), header))
            }

        }


    }


}