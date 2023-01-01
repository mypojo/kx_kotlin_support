package net.kotlinx.core2.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter

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

}