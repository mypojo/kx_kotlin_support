package net.kotlinx.aws.firehose

import net.kotlinx.aws.athena.table.AthenaTable

@Deprecated("LogData 사용하세요")
object HttpLogTable {

    /** 결과 파일 저장 샘플 - 아이스버그v2 */
    val HTTP_ICEBERG = AthenaTable {
        icebergTable()
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
    }

}