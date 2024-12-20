package net.kotlinx.csv

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

/** 라인 카운트 리턴 */
fun File.readCsvLinesCnt(charset: Charset = CharSets.UTF_8): Long {
    var count = 0L
    this.readCsvLines(charset) { row ->
        count++
    }
    return count
}




