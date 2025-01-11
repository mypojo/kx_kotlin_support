package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.string.CharSets
import java.io.File
import java.nio.charset.Charset

//==================================================== 쓰기 ======================================================

/** 간단하게 인메모리로 CSV 쓰기 */
fun File.writeCsvLines(lines: List<List<Any?>>, charset: Charset = CharSets.UTF_8) {
    csvWriter { this.charset = charset.toString() }.writeAll(lines, this)
}

//==================================================== 읽기 ======================================================

/**
 * 간단하게 인메모리로 CSV 읽기
 * @see readLines
 *  */
fun File.readCsvLines(charset: Charset = CharSets.UTF_8): List<List<String>> {
    return csvReader { this.charset = charset.toString() }.readAll(this)
}

/**
 * 간단하게 스트림으로 읽음
 * @see forEachLine -> csv 아니고, 일반 라인 읽기
 *  */
fun File.readCsvLines(charset: Charset = CharSets.UTF_8, callback: (List<String>) -> Unit) {
    val reader = csvReader { this.charset = charset.toString() }
    reader.open(this) {
        readAllAsSequence().forEach { row ->
            callback(row)
        }
    }
}

/**
 * CSV 파일을 읽어 첫 번째 라인을 헤더로 처리하고, 지정된 청크 크기만큼의 라인을 모아서 처리
 * @param charset 파일의 문자 인코딩 (기본값: UTF-8)
 * @param chunkSize 한 번에 처리할 라인의 수
 * @param callback 헤더와 청크 단위의 데이터를 처리하는 콜백 함수
 */
fun File.readCsvLinesWithHeaderAndChunk(
    reader: CsvReader = csvReader(),
    chunkSize: Int = 100,
    callback: (header: List<String>, rows: List<List<String>>) -> Unit
) {
    reader.open(this) {
        val iterator = readAllAsSequence().iterator()

        // 첫 번째 라인을 헤더로 처리
        if (!iterator.hasNext()) return@open
        val header = iterator.next()

        // 청크 단위로 데이터 처리
        val chunk = mutableListOf<List<String>>()
        while (iterator.hasNext()) {
            chunk.add(iterator.next())
            if (chunk.size == chunkSize) {
                callback(header, chunk.toList())
                chunk.clear()
            }
        }

        // 남은 데이터 처리
        if (chunk.isNotEmpty()) {
            callback(header, chunk.toList())
        }
    }
}


/** 라인 카운트 리턴 */
fun File.readCsvLinesCnt(charset: Charset = CharSets.UTF_8): Long {
    var count = 0L
    this.readCsvLines(charset) { row ->
        count++
    }
    return count
}




