package net.kotlinx.domain.item.tempData

import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.lambda.LambdaUtil
import java.time.LocalDateTime

/**
 * 임시로 사용할 간단 데이터. 랜덤 억세스만 지원함. (인덱스 XX)
 * ex) 람다 실행시, 반복실행을 막기위한 임시 마커
 * ex) 24시간만 유지되는 장바구니.
 */
data class TempData(
    override val pk: String,
    override val sk: String,

    /** 간단한 상태  */
    var status: String,

    /** 본문  */
    var body: String,

    /** 등록 시간  */
    var regTime: LocalDateTime = LocalDateTime.now(),

    /** TTL. */
    var ttl: Long = DynamoUtil.ttlFromNow(LambdaUtil.DEAFULT_TIMEOUT),

    ) : DbItem