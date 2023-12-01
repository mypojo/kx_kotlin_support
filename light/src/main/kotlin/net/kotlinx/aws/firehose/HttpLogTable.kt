package net.kotlinx.aws.firehose

import net.kotlinx.aws.athena.AthenaTable
import net.kotlinx.aws.athena.AthenaTableFormat
import net.kotlinx.aws.athena.AthenaTablePartitionType


object HttpLogTable {

    /** 결과 파일 저장 */
    val HTTP_RESULT = AthenaTable {
        tableName = "http_log"
        schema = mapOf(
            "basic_date" to "string",
            "name" to "string",
            "event_id" to "string",
            "metadata" to "string",
            "req_time" to "timestamp",
            "req_uri" to "string",
            "req_method" to "string",
            "req_header" to "string",
            "req_body" to "string",
            "resp_code" to "int",
            "resp_body" to "string",
        )
        partition = mapOf(
            "basic_date" to "string",
            "name" to "string",
        )
        athenaTableFormat = AthenaTableFormat.Parquet
        athenaTablePartitionType = AthenaTablePartitionType.PROJECTION
    }

}