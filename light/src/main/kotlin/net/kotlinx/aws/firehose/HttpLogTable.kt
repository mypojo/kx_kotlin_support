package net.kotlinx.aws.firehose

import net.kotlinx.aws.athena.table.AthenaTable
import net.kotlinx.aws.athena.table.AthenaTableFormat
import net.kotlinx.aws.athena.table.AthenaTablePartitionType


object HttpLogTable {

    /** 결과 파일 저장 샘플 */
    val HTTP_RESULT = AthenaTable {
        tableName = "http_log"
        schema = mapOf(
            "basic_date" PARTITION string,
            "name" PARTITION string,
            "event_id" to string,
            "metadata" to string,
            "req_time" to timestamp,
            "req_uri" to string,
            "req_method" to string,
            "req_header" to string,
            "req_body" to string,
            "resp_code" to int,
            "resp_body" to string,
        )
        athenaTableFormat = AthenaTableFormat.Parquet
        athenaTablePartitionType = AthenaTablePartitionType.INDEX
    }

}