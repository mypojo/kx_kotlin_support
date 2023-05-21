package net.kotlinx.kopring.opencsv

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter
import org.springframework.core.io.FileSystemResource
import java.io.File

/**
 * 입력 데이터를 변환된 ID (pk or date or  hash 등등)베이스로 분할 저장한다.
 * 오픈 스트림의 개수가 무한대여야 한다.
 * ex) 40G의 상품을 카테고리별 or 사용자별 or 날짜로 분할
 */
class CsvItemSplitWriter(
    /**
     * 한번에 최대로 오픈할 스트림 수. 파일 사이즈에 따라 가감해야 한다.
     * 쇼핑몰 상품 기준 500 -> 3.7G 사용
     * 설정이 있긴 하지만 최대한 사용하지 않도록 해시 수를 조절하자.
     * 최대로 오픈하지 못한 파일은 새 요청이 들어올때 어펜드된다.
     */
    private val splitLimit: Int = 500,

    /** 파일을 쌓아놓을 디렉토리 */
    private val workDir: File,

    /** CSV 라이터 팩토리 */
    private val csvItemWriterFactory: (File) -> CsvItemWriter = { file ->
        CsvItemWriter().apply {
            setResource(FileSystemResource(file))
            append = true
            utf8()
            open()
        }
    },

    /** ID 변환기 */
    private val toSplitKey: (Array<String>) -> String,
) : ItemWriter<Array<String>>, ItemStream {

    init {
        check(workDir.isDirectory) { "workDir must be directory" }
    }

    //==================== 내부사용 ========================
    /** 해시값 별로 CSV 라이터 사용 */
    private val writerMap: LinkedHashMap<String, CsvItemWriter> = LinkedHashMap()

    /** 작업 도중 limit을 넘어서 닫은 파일 수 */
    var closeFileCnt: Long = 0
        private set

    /** 처리한 아이템 수 */
    var totalItemCnt: Long = 0
        private set


    /**
     * 라이터가 없으면 스트림을 오픈해준다.
     */
    fun getOrMakeWriter(id: String): CsvItemWriter {
        synchronized(writerMap) {
            val writer: CsvItemWriter = writerMap[id] ?: run {
                val currentFile = File(workDir, "$id.csv")
                csvItemWriterFactory(currentFile).apply {
                    writerMap[id] = this
                }
            }
            //리밋에 도달했다면 하나 지움
            if (writerMap.size >= splitLimit) {
                val firstKet = writerMap.keys.stream().findFirst().get()
                val deleted: CsvItemWriter = writerMap.remove(firstKet)!!
                deleted.close()
                closeFileCnt++
            }
            return writer
        }
    }


    @Synchronized
    override fun write(items: List<Array<String>>) {
        val groupBy = items.groupBy { toSplitKey(it) }
        groupBy.entries.forEach { e ->
            val writer: CsvItemWriter = getOrMakeWriter(e.key)
            writer.write(e.value)
        }
        totalItemCnt += items.size
    }
    //=================================================== 오버라이드  ===================================================
    /** 아무것도 안함 */
    override fun open(executionContext: ExecutionContext) {}

    /** 아무것도 안함 */
    override fun update(executionContext: ExecutionContext) {}

    override fun close() = writerMap.values.forEach { it.close() }
}