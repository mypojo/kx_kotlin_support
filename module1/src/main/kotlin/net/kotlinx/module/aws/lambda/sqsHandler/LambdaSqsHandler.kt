package net.kotlinx.module.aws.lambda.sqsHandler

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.core.gson.GsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * SQS를 이벤트로 변환해준다ㅣ
 */
class LambdaSqsHandler : (GsonData) -> String?, KoinComponent {

    private val eventBus: EventBus by inject()

    override fun invoke(event: GsonData): String? {
        if (event["eventSource"].str != EVENT_SOURCE) return null

        val queueName = event["eventSourceARN"].str!!.substringAfterLast(":")
        val body: GsonData = GsonData.parse(event["body"].str!!)

        eventBus.post(LambdaSqsEvent(queueName, body))
        return LambdaUtil.Ok
    }

    companion object {
        private const val EVENT_SOURCE = "aws:sqs"
    }
}

private val sample = """
{
  "messageId": "29291a72-2c53-4bee-9cfd-11743595eb09",
  "receiptHandle": "AQEBHSw9KHoraJmyGybaP4UHIRhnEeLUaW5ZZmCaj9LDFDO809C+yaZ3UcGbc7/5U87fQ/zs8aWC6Lnozo7mBqVpqV/K4UY4nhrEgFfVfBL+xxUDB+8byr3cvPOaJqHKDVgLRmhfk0wQL77zyntMFB9O5Jybyl50oi7CEmGU5qrsWl1aLDJPJ6Vl7mP6vDdc1Bjydy2Hw/RjpoEDbDcaptwnKHwUkWTAP4Ar2pnqb9b+OKvCkxTcaN9eAaTXmmpURtXhc+6ItHoNWFa8t5lyNxrfXA0DUMA3iHJx1Zm1X08Gwav/UdongoxEgQQxcR2oMsJu3rZLnU2S71S6OsfXi/zahs/vbXID6wBdSCfG0BI5vERSPFeDuJ1Usuag/oKfO78Mvb4e2xg38K4wUlFv4Hi5bA==",
  "body": "{\"sqsDiv\":\"dirty\",\"campId\":998,\"mediaDiv2\":\"gdn\",\"regTime\":\"20220916165050\",\"source\":\"CampSynchSqsModuleTest\"}",
  "attributes": {
    "ApproximateReceiveCount": "1",
    "SentTimestamp": "1663314663262",
    "SenderId": "AROAZQNNA6UDGLOTIDQWL:sts",
    "ApproximateFirstReceiveTimestamp": "1663314663267"
  },
  "messageAttributes": {},
  "md5OfBody": "a21470f7fe7262ec2b75c82d8c303875",
  "eventSource": "aws:sqs",
  "eventSourceARN": "arn:aws:sqs:ap-northeast-2:653734769926:sin-synch_gdn-dev",
  "awsRegion": "ap-northeast-2"
}    
""".trimIndent()
