package net.kotlinx.aws.lambda.dispatch.asynch

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.json.gson.GsonData

/**
 * SQS 본문 내용으로 트리거
 * SQS가 직접 람다를 트리거하는경우 람다에서 따로 SQS를 지우지 않아도 자동으로 지워짐 (receiptHandle 필요없음)
 * */
data class AwsSqsEvent(val messageId: String, val receiptHandle: String, val sqsName: String, val body: GsonData) : AwsLambdaEvent