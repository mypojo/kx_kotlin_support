import com.google.common.collect.Lists
import net.kotlinx.kopring.opencsv.CsvItemReader

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
 * ex) CsvItemReader.of(readFile).utf8().open().count()
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