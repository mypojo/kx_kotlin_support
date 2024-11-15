package net.kotlinx.aws.dynamo

import net.kotlinx.aws.AwsConfig
import net.kotlinx.string.encodeUrl
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 * ttl은 초단위
 * */
object DynamoUtil {

    /**
     * 지금 기준 X일 이후 지정. TTL은 초단위 이다.
     * 이건 자바용으로 남겨둠
     *  */
    fun ttlFromNow(timeUnit: TimeUnit, interval: Long): Long {
        val ttlSec: Long = timeUnit.toSeconds(interval)
        return System.currentTimeMillis() / 1000 + ttlSec
    }

    /** 지금 기준 X일 이후 지정. TTL은 초단위 이다.  */
    fun ttlFromNow(duration: Duration): Long = System.currentTimeMillis() / 1000 + duration.inWholeSeconds

    /** item 바로가기 링크 */
    fun toItemLink(tableName: String, pk: String, sk: String, region: String = AwsConfig.REGION_KR): String {
        return "https://${region}.console.aws.amazon.com/dynamodbv2/home?region=${region}#edit-item?table=${tableName}&itemMode=2&pk=${pk.encodeUrl()}&sk=${sk.encodeUrl()}&route=ROUTE_ITEM_EXPLORER"
    }

    /** item 쿼리 링크 */
    fun toItemQuery(tableName: String, pk: String, sk: String, region: String = AwsConfig.REGION_KR, compare: String = "BEGINS_WITH"): String {
        return "https://${region}.console.aws.amazon.com/dynamodbv2/home?region=${region}#item-explorer?operation=QUERY&pk=${pk.encodeUrl()}&sk=${sk.encodeUrl()}&skComparator=${compare}&table=${tableName}"
    }

}