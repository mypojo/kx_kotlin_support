package net.kotlinx.file

import kotlinx.coroutines.flow.FlowCollector
import net.kotlinx.core.Kdsl
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * 문자열로 파일을 기록하는 간이도구
 * csv가 아닌 xml 등을 기록할때 사용함
 *  */
class FileLineCollector : FlowCollector<List<String>>, AutoCloseable {

    @Kdsl
    constructor(block: FileLineCollector.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** 파일 인코딩 */
    var encoding: String = "UTF-8"

    /** 파일 */
    lateinit var file: File

    //==================================================== 내부사용 ======================================================

    private lateinit var writer: BufferedWriter

    /**
     * ex) xml 헤더 입력
     * */
    open fun open() {
        writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file), encoding), BUFFER_SIZE)
    }

    override suspend fun emit(lines: List<String>) {
        lines.forEach { writer.appendLine(it) }
        writer.flush()
    }

    open override fun close() {
        writer.flush()
        writer.close()
    }

    companion object {
        private const val BUFFER_SIZE = 8192  // 8KB 버퍼
    }


}