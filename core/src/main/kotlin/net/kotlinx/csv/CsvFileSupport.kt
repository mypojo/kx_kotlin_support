package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.string.CharSets
import java.io.File
import java.nio.charset.Charset


/** 간단하게 인메모리로 CSV 읽기 */
fun File.readCsvLines(charset: Charset = CharSets.UTF_8): List<List<String>> {
    return csvReader { this.charset = charset.toString() }.readAll(this)
}

/** 간단하게 인메모리로 CSV 쓰기 */
fun File.writeCsvLines(lines: List<List<Any?>>, charset: Charset = CharSets.UTF_8) {
    csvWriter { this.charset = charset.toString() }.writeAll(lines, this)
}

/** 간단하게 스트림으로 읽음 */
fun File.readCsvLines(charset: Charset = CharSets.UTF_8, callback: (List<String>) -> Unit) {
    val reader = csvReader { this.charset = charset.toString() }
    reader.open(this) {
        readAllAsSequence().forEach { row ->
            callback(row)
        }
    }
}

/** 간단하게 인코딩 변경해서 재저장 */
fun File.readAndwriteCsvToCharset(
    /** 결과파일 */
    outFile: File,
    /** 이 파일의 인코딩 */
    thisFileCharset: Charset = CharSets.UTF_8,
    /** 새로 만들 파일의 인코딩 */
    toCharset: Charset = CharSets.MS949,
    /** 커스텀 */
    block: (List<String>) -> List<String> = { it }
): File {
    val reader = csvReader { charset = thisFileCharset.toString() }
    val writer = csvWriter { charset = toCharset.toString() }
    reader.open(this) {
        writer.open(outFile) {
            readAllAsSequence().forEach { line ->
                writeRow(block(line))
            }
        }
    }
    return outFile
}



