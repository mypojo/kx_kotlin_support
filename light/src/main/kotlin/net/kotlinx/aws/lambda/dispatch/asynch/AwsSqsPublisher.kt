package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * SQS 본문 내용으로 트리거
 * SQS가 직접 람다를 트리거하는경우 람다에서 따로 SQS를 지우지 않아도 자동으로 지워짐 (receiptHandle 필요없음)
 * */
data class SqsEvent(val messageId: String, val receiptHandle: String, val sqsName: String, val body: GsonData) : AwsLambdaEvent

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
        return bus.postEvent { SqsEvent(messageId, receiptHandle, sqsName, sqsBody) }
    }

}
