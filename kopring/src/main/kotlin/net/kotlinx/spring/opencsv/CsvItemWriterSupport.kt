package net.kotlinx.spring.opencsv

import jakarta.servlet.http.HttpServletResponse
import net.kotlinx.spring.resource.OutputStreamResource2
import org.springframework.batch.item.Chunk

/**
 * 인메모리용 CSV 라이터 -> http resp 에 직접 write
 * SQL이나 로직으로 CSV 파일을 다운로드할때 사용
 */
fun HttpServletResponse.toCsvItemWriter(): CsvItemWriter {
    this.contentType = "application/octet-stream"
    this.setHeader("Content-Transfer-Encoding", "binary")
    val resp = this
    return CsvItemWriter().apply {
        setResource(OutputStreamResource2(resp.outputStream))
    }
}


/** 간단 쓰기 */
fun CsvItemWriter.writeAll(items: List<Array<String>>) {
    open()
    write(Chunk(items))
    close()
}