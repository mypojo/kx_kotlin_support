package net.kotlinx.core.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

typealias Line = List<String>

/**
 * 코틀린 오피셜 CSV
 * 코드 참고용
 * https://github.com/doyaaaaaken/kotlin-csv
 * */
object CsvUtil {

    /** 한글 엑셀 리더 */
    inline fun ms949Reader(): CsvReader = csvReader { charset = "MS949"; delimiter = ',' }

    /** 한글 엑셀 리더 */
    inline fun ms949Writer(): CsvWriter = csvWriter { charset = "MS949"; delimiter = ',' }

    /** gzip 파일 간단 쓰기 ex) S3 athena */
    fun writeAllGzip(file: File, lines: List<List<Any?>>) {
        GZIPOutputStream(FileOutputStream(file)).use { gos ->
            csvWriter().writeAll(lines, gos)
        }
    }


}