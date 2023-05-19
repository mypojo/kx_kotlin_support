package net.kotlinx.kopring.opencsv

import com.google.common.collect.Lists
import net.kotlinx.kopring.spring.resource.toResource
import java.io.File

/** 간단읽기 */
fun CsvItemReader.readAll(): List<Array<String>> {
    val list: MutableList<Array<String>> = Lists.newArrayList()
    try {
        while (true) {
            val data = this.read() ?: break
            list.add(data)
        }
    } finally {
        close()
    }
    return list
}

/**
 * 메모리에 누적하지 않고 숫자만 셀때
 * ex) CsvItemReader.of(readFile).utf8().open().net.kotlinx.kopring.opencsv.count()
 */
fun CsvItemReader.count(): Long {
    var cnt: Long = 0
    try {
        while (true) {
            this.read() ?: break
            cnt++
        }
    } finally {
        close()
    }
    return cnt
}

/**
 * 간단 샘플은 이정도 까지만..
 * 시스템에서 만든 문서라면 urf8() 호출할것
 *  */
fun File.toCsvReader(linesToSkip: Int = 1): CsvItemReader {
    val file = this
    return CsvItemReader().apply {
        setResource(file.toResource())
        this.linesToSkip = linesToSkip
    }
}


