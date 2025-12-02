package net.kotlinx.notion

import mu.KotlinLogging
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.domain.item.tempData.TempData
import net.kotlinx.domain.item.tempData.TempDataRepository
import net.kotlinx.json.gson.json
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.string.toLocalDateTime
import net.kotlinx.time.toIso
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

private const val PK = "notion"
private const val SK = "lastUpdateTime"
private val log = KotlinLogging.logger {}

/**
 * TempData 관련 확장 함수 모음
 */
suspend fun TempDataRepository.putLastUpdate(time: LocalDateTime) {
    val item = TempData(
        pk = PK,
        sk = SK,
        status = "non",
        body = json {
            SK to time.toIso()
        }.toString(),
        ttl = DynamoUtil.ttlFromNow(5.minutes)
    )
    putItem(item)
}


suspend fun TempDataRepository.findLastUpdate(): LocalDateTime {
    val exist = this.getItem(PK, SK)
    return if (exist == null) {
        // 1년전 시간을 입력
        log.warn { "데이터를 찾을 수 없어서 1년전 값으로 리턴함!" }
        val updated = LocalDateTime.now().minusYears(1)!!
        val tempData = TempData(
            pk = PK,
            sk = SK,
            status = "non",
            body = json {
                SK to updated.toIso()
            }.toString(),
            ttl = DynamoUtil.ttlFromNow(5.minutes)
        )
        this.putItem(tempData)
        updated
    } else {
        exist.body.toGsonData()[SK].str!!.toLocalDateTime()
    }
}