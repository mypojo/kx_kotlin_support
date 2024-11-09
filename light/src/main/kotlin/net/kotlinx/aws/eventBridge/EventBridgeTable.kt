package net.kotlinx.aws.eventBridge

import net.kotlinx.aws.athena.table.AthenaTable
import net.kotlinx.aws.athena.table.AthenaTableFormat
import net.kotlinx.aws.athena.table.AthenaTablePartitionType


object EventBridgeTable {

    /** 이벤트브릿지 로그 */
    val EVENTBRIDGE_LOG = AthenaTable {
        tableName = "eventbridge"
        schema = mapOf(
            "basic_date" PARTITION string,
            "source" PARTITION string,
            "id" to string,
            "detail-type" to string,
            "account" to string,
            "time" to timestamp,
            "region" to string,
            "resources" to arrayString,
            "detail" to string,
        )
        athenaTableFormat = AthenaTableFormat.Parquet
        athenaTablePartitionType = AthenaTablePartitionType.INDEX
    }

}