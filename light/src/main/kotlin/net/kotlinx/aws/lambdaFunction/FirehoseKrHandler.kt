package net.kotlinx.aws.lambdaFunction

import aws.sdk.kotlin.services.s3.copyObject
import aws.sdk.kotlin.services.s3.deleteObject
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.s3.s3
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.string.toLocalDateTime
import net.kotlinx.time.toH
import net.kotlinx.time.toYmd
import java.net.URLDecoder

/**
 * 파이어호스 저장 시간을 한국시간으로 교체
 * CDK에 프리픽스 베이스로 S3 이벤트를 설정해야 한다.
 * net.kotlinx.aws1.s3.FirehoseKrHandler::handleRequest
 *
 * https://gist.github.com/sawyerh/f809cc6d539c54287fc87223fc6c0f9b
 *
 * @see net.kotlinx.aws.lambdaCommon.CommonFunctionHandler 단독 매핑 보다는 이걸 우선 사용할것
 */
class FirehoseKrHandler : RequestHandler<S3Event, String> {

    private val log = KotlinLogging.logger {}

    private val pathFrom = System.getenv("PATH_FROM") ?: ""
    private val pathTo = System.getenv("PATH_TO") ?: ""

    init {
        log.info { "[람다 초기화] 설정 : $pathFrom => $pathTo" }
    }

    private val matcherbasicDate = "(?<=/basicDate=).*?(?=/)".toRegex()
    private val matcherHh = "(?<=/hh=).*?(?=/)".toRegex()
    private val matcherPrefix = "^.*?(?=/)".toRegex()

    private val aws = AwsConfig().toAwsClient1()

    override fun handleRequest(event: S3Event, context: Context?): String {

        event.records.map { it.s3 }.forEach { s3 ->

            val bucketName = s3.bucket.name
            val oldKey = URLDecoder.decode(s3.`object`.key, Charsets.UTF_8.name())

            val basicDate = matcherbasicDate.find(oldKey)!!.value
            val hh = matcherHh.find(oldKey)!!.value.toInt()

            val utcTime = "$basicDate$hh".toLocalDateTime()
            val koreanTime = utcTime.plusHours(9)
            log.debug { "update $utcTime => $koreanTime" }

            val newBasicDate = koreanTime.toLocalDate().toYmd()
            val newHh = koreanTime.toLocalTime().toH() //패딩이 되어있음
            val newKey = oldKey.replace(matcherbasicDate, newBasicDate).replace(matcherHh, newHh).replace(matcherPrefix, pathTo)

            runBlocking {
                aws.s3.copyObject {
                    copySource = "$bucketName/$oldKey"  //CopySource 에는 버킷 이름을 같이 넣어줘야함
                    bucket = bucketName
                    key = newKey
                }
                aws.s3.deleteObject {
                    bucket = bucketName
                    key = oldKey
                }
            }
            log.info { "KDF 업데이트 성공 $oldKey => $newKey" }
        }
        return "ok"
    }
}