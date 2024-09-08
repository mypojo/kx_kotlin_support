package net.kotlinx.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.kotlinx.core.Kdsl
import net.kotlinx.string.CharSets
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.GZIPOutputStream


/**
 * 파일을 즉시 읽고 다시 써주는 간이도구
 * 람다에서 800mb 가량의 데이터 처리 시도시 에러난다.. 디스크 문제일수도 있음
 *  */
class CsvReadWriteTool {

    @Kdsl
    constructor(block: CsvReadWriteTool.() -> Unit = {}) {
        apply(block)

        val reader = csvReader { charset = inFileCharset.toString() }
        val writer = csvWriter { charset = outFileCharset.toString() }

        reader.open(inInputStream) {
            val stream = if (gzip) GZIPOutputStream(FileOutputStream(outFile)) else FileOutputStream(outFile)
            writer.open(stream) {
                readAllAsSequence().forEach { line ->
                    writeRow(processor(line))
                }
            }
        }
    }

    /** 입력파일 */
    var inFile: File? = null
        set(value) {
            inInputStream = value!!.inputStream()
            field = value
        }

    /** 입력파일 스트림 */
    lateinit var inInputStream: InputStream

    /** 입력파일 인코딩 */
    var inFileCharset: Charset = CharSets.UTF_8

    /** 결과파일 */
    lateinit var outFile: File

    /** 결과파일 인코딩 */
    var outFileCharset: Charset = CharSets.UTF_8

    /** 압축 여부 */
    var gzip: Boolean = false

    /** 라인단위 처리 */
    var processor: (List<String>) -> List<String> = { it }


}

