package net.kotlinx.file

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import net.kotlinx.io.input.InputResource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset


/**
 * 파일을 라인 단위로 읽어서 Flow로 변환
 * 반드시 라인 세퍼레이터가 있어야함!! (이게 없는 xml 등은 못읽음)
 * ex) G마켓 상품정보 등의 csv도, xml도 아닌 이상한 대량 파일 커스텀 읽기
 */
fun InputResource.toFlowLine(charset: Charset = Charsets.UTF_8): Flow<String> {
    val input = this
    return flow {
        BufferedReader(InputStreamReader(input.inputStream, charset)).use { reader ->
            reader.lineSequence().forEach { line -> emit(line) }
        }
    }
}

/**
 * 다수의 파일을 하나의 플로우로 변환 (라인 단위)
 * 코드 참고용
 */
fun List<InputResource>.toFlowLine(charset: Charset = Charsets.UTF_8): Flow<String> =
    this.map { it.toFlowLine(charset) }.asFlow().flattenConcat()