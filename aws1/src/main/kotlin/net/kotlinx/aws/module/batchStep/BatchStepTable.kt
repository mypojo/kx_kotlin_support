package net.kotlinx.aws.module.batchStep

import net.kotlinx.aws.athena.AthenaTable
import net.kotlinx.aws.athena.AthenaTableFormat
import net.kotlinx.aws.athena.AthenaTablePartitionType

/**
 * 아테나 테이블 정의
 * */
object BatchStepTable {

    /** 결과 파일 저장 */
    val result = AthenaTable {
        schema = mapOf(
            "sfn_id" to "string",
            "file_name" to "string",
            "total_interval" to "bigint",
            "total_size" to "int",
            "input" to "string",
            "output" to "string",
            "interval" to "bigint",
        )
        partition = mapOf(
            "sfn_id" to "string",
        )
        athenaTableFormat = AthenaTableFormat.Json
        athenaTablePartitionType = AthenaTablePartitionType.Projection
    }


}


