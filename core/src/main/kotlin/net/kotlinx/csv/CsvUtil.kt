package net.kotlinx.csv

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
    fun ms949Reader(): CsvReader = csvReader { charset = "MS949"; delimiter = ',' }

    /** 한글 엑셀 리더 */
    fun ms949Writer(): CsvWriter = csvWriter { charset = "MS949"; delimiter = ',' }

    /** gzip 파일 간단 쓰기 ex) S3 athena */
    fun writeAllGzip(file: File, lines: List<List<Any?>>) {
        GZIPOutputStream(FileOutputStream(file)).use { gos ->
            csvWriter().writeAll(lines, gos)
        }
    }

    /**
     * 짭 tsv 파싱용
     * */
    val TSV_UNOFFICIAL: CsvReader = csvReader {
        delimiter = '\t' //탭으로 구분 (TSV)
        //TSV 표준을 따르지 않는경우, 사용안하는 캐릭터 아무거나 하나 지정
        escapeChar = Char(1)
        quoteChar = Char(1)
        skipMissMatchedRow = true //이게 있으면 잘못된 라인 스킵 ex) 상품명에 탭이 들어감
    }


}