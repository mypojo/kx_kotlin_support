package net.kotlinx.aws.firehose.logData

import net.kotlinx.aws.athena.table.AthenaTable

object LogDataTable {

    /**
     * CDK에서 키네시스 매핑할때 사용함
     * 이게 있어야 업데이트 or 삭제가 가능함
     *  */
    val UNIQUE_KEY = listOf(
        "basic_date",
        "project_name",
        "event_div",
        "event_id",
    )

    /** 결과 파일 저장 샘플 - 아이스버그v2 */
    val LOG_DATA = AthenaTable {
        icebugTable()
        tableName = "log_data"
        tableComment = "통합 로그 데이터"
        schema = mapOf(
            "basic_date" PARTITION string,
            "project_name" PARTITION string,
            "event_div" to string,
            "event_id" to string,
            "event_time" to timestamp,
            "instance_type" to string,
            "event_name" to string,
            "event_desc" to string,
            "event_status" to string,
            "event_mills" to int,
            "member_id" to string,
            "g1" to string,
            "g2" to string,
            "g3" to string,
            "keyword" to string,
            "x" to string,
            "y" to string,
            "metadata" to string,

            )
    }

}