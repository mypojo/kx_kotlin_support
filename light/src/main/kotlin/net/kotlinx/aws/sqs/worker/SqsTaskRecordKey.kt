package net.kotlinx.aws.sqs.worker

/**
 * SQS 메시지 키 기반으로 작동한다
 * */
data class SqsTaskRecordKey(val taskName: String, val taskId: String, val recordId: String) {

    val inMessageId = "${taskName}-$taskId-$recordId-in"

    val outMessageId = "${taskName}-$taskId-$recordId-out"

    /** 이하 네이밍 맞춤 */
    companion object {

        fun taskFlowName(taskName: String, taskId: String) = "$taskName-$taskId"

        fun parse(messageId: String): SqsTaskRecordKey {
            val split = messageId.split("-")
            return SqsTaskRecordKey(split[0], split[1], split[2])
        }

        fun isOut(messageId: String): Boolean = messageId.endsWith("-out")
        fun isIn(messageId: String): Boolean = messageId.endsWith("-in")

    }

}