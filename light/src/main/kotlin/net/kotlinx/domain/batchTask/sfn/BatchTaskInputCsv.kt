package net.kotlinx.domain.batchTask.sfn

import com.lectra.koson.Koson
import net.kotlinx.csv.chunkTools.CsvReadWriteTools

/**
 * SFN 입력데이터 (대용량)
 * */
data class BatchTaskInputCsv(
    val csvReadWriteTools: CsvReadWriteTools,
    val batchTaskIds: List<String>,
    var inputOptionBlock: Koson.() -> Unit = {}
)