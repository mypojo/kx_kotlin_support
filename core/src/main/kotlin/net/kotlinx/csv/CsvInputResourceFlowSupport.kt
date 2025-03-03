package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import net.kotlinx.io.input.InputResource


/** 일반적으로 처리하는 청크단위 라인들 */
typealias Lines = List<List<String>>

//==================================================== 단일파일 ======================================================

/**
 * CSV 파일 등을 간단히 플로우로  변환
 * ex) 대량의 데이터를 청크로 나누어 처리
 *  */
fun InputResource.toFlow(reader: CsvReader = csvReader()): Flow<List<String>> {
    val input = this
    return flow { reader.openAsync(input.inputStream) { readAllAsSequence().forEach { emit(it) } } }
}

/**
 * 헤더 있는 버전
 * */
fun InputResource.toFlowWithHeader(reader: CsvReader = csvReader()): Flow<Map<String, String>> {
    val input = this
    return flow { reader.openAsync(input.inputStream) { readAllWithHeaderAsSequence().forEach { emit(it) } } }
}

//==================================================== 멀티 파일 ======================================================


/**
 * 다수의 파일을 하나의 플로우로 변환
 * 멀티 리소스 리더와 동일
 * */
fun List<InputResource>.toFlow(reader: CsvReader = csvReader()): Flow<List<String>> = this.map { it.toFlow(reader) }.asFlow().flattenConcat()

/**
 * 헤더 있는 버전
 * */
fun List<InputResource>.toFlowWithHeader(reader: CsvReader = csvReader()): Flow<Map<String, String>> = this.map { it.toFlowWithHeader(reader) }.asFlow().flattenConcat()
