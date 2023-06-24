package net.kotlinx.spring.opencsv

import com.opencsv.CSVParser
import com.opencsv.CSVWriter
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * 스래드 안전하긴 하지만 병렬 처리시 write 되는 순서를 보장하지 못한다.
 */
class CsvItemWriter : ResourceAwareItemWriterItemStream<Array<String>>, ItemWriter<Array<String>>, ItemStream {

    //====================== 설정파일 ============================
    /** 리소스 */
    private lateinit var resource: Resource

    override fun setResource(resource: Resource) {
        this.resource = resource
    }

    /** 기본은 엑셀로 읽을 수 있는 MS949 */
    var encoding = "MS949"

    /** 1.수동 헤더 */
    var header: Array<String>? = null

    /** 플러싱 하는 버퍼 사이즈 */
    var bufferSize = 1024 * 1024

    /** true이면 뒤에 붙여쓴다 */
    var append = false

    /**
     * 이스케이핑을 할것인지 여부(인거 같다.). CSV로 write한것을 다시 CSV로 읽으려면 동일한 이스케이퍼(\)를 사용해야 한다.
     * 대신 이렇게 이스케이핑 하면 MS의 엑셀 프로그램으로 읽지 못한다. (이스케이핑 문자 \가 그대로 보여진다.)
     * 읽을때는 이스케이핑을 자동으로 하기때문에 이스케이핑 하지 않을경우 데이타 내에 들어간 \\문자가 실제 \로 읽힌다. 주의!
     */
    var escape = false

    /** 가끔 tap \t 으로 요구할때도 있다. */
    var separator = CSVWriter.DEFAULT_SEPARATOR

    /** "" 로 묶이는거 싫으면 직접 설정 */
    var quote = CSVWriter.DEFAULT_QUOTE_CHARACTER

    /**
     * 라인 세퍼레이터
     */
    var lineEnd = "\n"

    //====================== 내부 사용 ============================
    /** 오픈되어있음 */
    lateinit var writer: CSVWriter
    var lineCount = 0

    /**
     * 이게 문제되지는 않겠지? ㅎㅎ
     */
    val mutex: Lock = ReentrantLock(false)

    //==================================================== 간단 ======================================================

    /** 간단 오픈 */
    fun open(): CsvItemWriter {
        this.open(ExecutionContext())
        return this
    }

    /** 간단 변환 */
    fun utf8(): CsvItemWriter {
        escape = true
        encoding = "UTF-8"
        return this
    }

    //==================================================== 구현 ======================================================

    /**
     * 확실히 버퍼는 작동하는듯 하다. F5 연타하면 깔끔하게 1메가씩 올라간다.
     * 근데 성능 차이는 없는거 같다.. (확인은 안해봄)
     */
    override fun open(executionContext: ExecutionContext) {
        writer = run {
            check(resource is WritableResource) { "WritableResource is required" }
            val writableResource = resource as WritableResource
            val out = if (append) FileOutputStream((writableResource as FileSystemResource?)!!.file, append) else writableResource.outputStream
            val w = OutputStreamWriter(out, encoding)
            val ww = BufferedWriter(w, bufferSize) //디폴트가 8192 일듯
            val escaper = if (escape) CSVParser.DEFAULT_ESCAPE_CHARACTER else CSVWriter.DEFAULT_ESCAPE_CHARACTER
            CSVWriter(ww, separator, quote, escaper, lineEnd)
        }
        header?.let {
            write(listOf(it))
        }
    }

    override fun update(executionContext: ExecutionContext) {
        executionContext.putInt(WRITE_COUNT, lineCount)
    }

    /**
     * 닫으면서 flush 하는것으로 추청된다.
     */
    override fun close() {
        writer.close()
    }


    /**
     * SQL의 메타데이터는 read()후에 계산됨으로 header에 쓰는 부분을 여기에 둔다
     */
    override fun write(items: List<Array<String>>) {
        mutex.lock()
        try {
            for (item in items) {
                lineCount++
                writer.writeNext(item)
            }
        } finally {
            mutex.unlock()
        }
    }

    companion object {
        const val WRITE_COUNT = "write.net.kotlinx.kopring.opencsv.count"
    }
}