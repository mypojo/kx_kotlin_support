package net.kotlinx.core.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.kotlinx.core.string.CharSets
import java.io.File
import java.nio.charset.Charset


/** 간단하게 인메모리로 CSV 읽기 */
fun File.readCsvLines(charset: Charset = CharSets.UTF_8): List<List<String>> {
    return csvReader { this.charset = charset.toString(); delimiter = ',' }.readAll(this)
}
