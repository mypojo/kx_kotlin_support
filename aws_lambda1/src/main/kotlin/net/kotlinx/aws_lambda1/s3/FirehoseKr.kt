package net.kotlinx.aws_lambda1.s3

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import mu.KotlinLogging
import net.kotlinx.kotlinSupport.string.toLocalDateTime
import net.kotlinx.kotlinSupport.time.toH
import net.kotlinx.kotlinSupport.time.toYmd
import java.net.URLDecoder

/**
 * 네이밍룰  {패키지}/{이벤트타입}/{목적}
 * net.kotlinx.aws_lambda1.s3.FirehoseKr::handleRequest
 * 파이어호스 저장 시간을 한국시간으로 교체
 */
class FirehoseKr : RequestHandler<S3Event, String> {

    private val log = KotlinLogging.logger {}

    private val version = "1"

    private val pathFrom = System.getenv("PATH_FROM")
    private val pathTo = System.getenv("PATH_TO")

    init {
        log.info { "[람다 초기화 v$version] 설정 : $pathFrom => $pathTo" }
    }

    val MATCH_BASIC_DATE = "(?<=\\/basicDate=).*?(?=\\/)".toRegex()
    val MATCH_HH = "(?<=\\/hh=).*?(?=\\/)".toRegex()
    //val MATCH_SUFFIX = "^.*?(?=\\/)".toRegex()
    val MATCH_PREFIX = "^.*?(?=\\/)".toRegex()

    override fun handleRequest(event: S3Event, context: Context?): String {

        event.records.map { it.s3 }.forEach { s3 ->

            val bucketName = s3.bucket.name
            val key = URLDecoder.decode(s3.`object`.key, Charsets.UTF_8.name())

            val basicDate = MATCH_BASIC_DATE.find(key)!!.value
            val hh = MATCH_HH.find(key)!!.value.toInt()

            val utcTime = "$basicDate$hh".toLocalDateTime()
            val koreanTime = utcTime.plusHours(9)
            log.debug { "update $utcTime => $koreanTime" }

            val newBasicDate = koreanTime.toLocalDate().toYmd()
            val newHh = koreanTime.toLocalTime().toH()
            val newKey = key.replace(MATCH_BASIC_DATE, newBasicDate).replace(MATCH_HH, newHh).replace(MATCH_PREFIX, pathTo)

            log.debug { "key    $key" }
            log.debug { "newKey $newKey" }

//
//            //별도의 처리를 하지 않기 위해서 async 하지 않게 코딩
//            //https://docs.aws.amazon.com/lambda/latest/dg/nodejs-handler.html
//            //CopySource 에는 버킷 이름을 같이 넣어줘야함
//
//            await s3.copyObject({Bucket: bucketName, CopySource: bucketName + '/' + key, Key: newKey}).promise();
//            await s3.deleteObject({Bucket: bucketName, Key: key}).promise();
//            console.info(`KDF 업데이트 성공 3 : ${key} => ${newKey}`)

        }

        return "ok v${version}"
    }
}