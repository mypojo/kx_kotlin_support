package net.kotlinx.kopring.opencsv

import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream
import org.springframework.core.io.Resource
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.io.Reader

/**
 * csvMapper에서 매핑하는데 시간이 많이 걸릴것으로 예상된다면(그럴일이 많이 없겠지만) PassThroughCsvMapper를 일단 사용한 후 processor에서 변환하도록 하자.
 * FlatFileReader와 다른점은 텍스트 안에 \n가 들어있어도 정상적으로 파일을 읽는다.
 */
class CsvItemReader : ResourceAwareItemReaderItemStream<Array<String>>, ItemReader<Array<String>>, ItemStream {

    //=========================== 설정파일 ===============================
    /** 리소스  */
    private lateinit var resource: Resource

    /** 기본으로 그대로 통과 넣어줌.  */
    var encoding = "MS949"
    var linesToSkip = 0

    /**  [org.springframework.batch.item.file.FlatFileItemReader] 그대로 복사했다.  */
    var maxItemCount = Int.MAX_VALUE

    /** 비어있는 리소스를 읽어서 빈값을 리턴할때 사용  */
    var openSkip = false

    //=========================== 내부사용 ===============================
    lateinit var reader: CSVReader
    var lineCount = 0

    //=========================== 편의 메소드 ===============================
    fun utf8(): CsvItemReader {
        this.encoding = "UTF-8"
        return this
    }

    /** 간단 오픈  */
    fun open(): CsvItemReader {
        this.open(ExecutionContext())
        return this
    }

    //==================================================== 구현 ======================================================

    override fun close() = reader.close()

    override fun open(context: ExecutionContext) {
        if (openSkip) {
            reader = object : CSVReader(EmptyReader()) {
                override fun readNext(): Array<String>? = null
            }
            return
        }
        val inputStreamReader = InputStreamReader(BufferedInputStream(resource.inputStream), encoding)
        reader = CSVReaderBuilder(inputStreamReader).withSkipLines(linesToSkip).build()
    }

    override fun update(context: ExecutionContext) {
        context.putInt(READ_COUNT, lineCount)
    }

    /** 자료가 없으면 카운트를 올리지 않는다.  */
    @Synchronized
    override fun read(): Array<String>? {
        if (lineCount >= maxItemCount) {
            return null
        }
        val lines: Array<String> = reader.readNext()
        if (lines != null) lineCount++
        return lines
    }

    //=========================== 보조 클래스 ===============================
    /** 아무것도 안한다.  */
    class EmptyReader : Reader() {
        override fun read(cbuf: CharArray, off: Int, len: Int): Int {
            return 0
        }

        override fun close() {
        }
    }


    //=========================== 인터페이스 구현 ===============================
    override fun setResource(resource: Resource) {
        this.resource = resource
    }

    companion object {
        const val READ_COUNT = "read.net.kotlinx.kopring.opencsv.count"
    }
}