package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.kotlinx.io.input.InputResource


/**
 * CSV 파일 등을 간단히 플로우로  변환
 * ex) 대량의 데이터를 청크로 나누어 처리
 *  */
fun InputResource.toFlow(reader: CsvReader = csvReader()): Flow<List<String>> {
    val input = this
    return flow {
        reader.openAsync(input.inputStream) {
            readAllAsSequence().forEach { row ->
                emit(row)
            }
        }
    }
}
