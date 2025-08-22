package net.kotlinx.spring.mvc

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


/**
 * spring 스타일로 응답을 변환해준다
 * 인코딩은 일단 무시함
 * */
fun CsvWriter.writeToSpring(filename: String, lines: List<List<String>>): ResponseEntity<StreamingResponseBody> {
    val streamingResponseBody = StreamingResponseBody { outputStream ->
        this.open(outputStream) { lines.forEach { row -> writeRow(row) } }
    }
    val encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20")
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFileName")
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .body(streamingResponseBody)
}