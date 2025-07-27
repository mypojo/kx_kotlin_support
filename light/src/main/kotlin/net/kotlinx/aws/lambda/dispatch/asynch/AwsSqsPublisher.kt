package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * SQS에 데이터 입력 -> 람다 트리거
 * ex) 특정 로직 실행..
 */
class AwsSqsPublisher : LambdaDispatch {

    private val bus by koinLazy<EventBus>()

    companion object {
        const val SOURCE_SQS = "aws:sqs"
    }

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        if (input[AwsNaming.Event.EVENT_SOURCE].str != SOURCE_SQS) return null

        val messageId: String = input["messageId"].str!!
        val receiptHandle: String = input["receiptHandle"].str!!
        val sqsName: String = input["eventSourceARN"].str!!.substringAfterLast(":")
        val body: String = input["body"].str ?: "{}"
        val sqsBody: GsonData = GsonData.parse(body)
        return bus.postEvent { AwsSqsEvent(messageId, receiptHandle, sqsName, sqsBody) }
    }

}
