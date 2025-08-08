package net.kotlinx.aws.kinesis.worker

/**
 * 파티션 키 기반으로 작동한다
 * */
data class KinesisTaskRecordKey(val taskName: String, val taskId: String, val recordId: String) {

    val inPartitionKey = "${taskName}-$taskId-$recordId-in"

    val outPartitionKey = "${taskName}-$taskId-$recordId-out"

    /** 이하 네이밍 맞춤 */
    companion object {

        fun taskFlowName(taskName: String, taskId: String) = "$taskName-$taskId"

        fun parse(partitionKey: String): KinesisTaskRecordKey {
            val split = partitionKey.split("-")
            return KinesisTaskRecordKey(split[0], split[1], split[2])
        }

        fun isOut(partitionKey: String): Boolean = partitionKey.endsWith("-out")
        fun isIn(partitionKey: String): Boolean = partitionKey.endsWith("-in")

    }

}