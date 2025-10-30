package net.kotlinx.aws.dynamo

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

/**
 * DynamoDb 관련 유틸 테스트
 * - increaseMapValueAndGet 만 검증용 샘플 호출 작성
 * - 실제 호출은 IGNORE 설정으로 기본적으로 실행되지 않음
 */
class DynamoDbSupportTest : BeSpecHeavy() {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    init {
        // 기본적으로 테스트 무시 (실행 필요시 적절히 변경)
        initTest(KotestUtil.IGNORE)

        Given("increaseMapValueAndGet 동작 확인") {

            val aws = koin<AwsClient>(findProfile97)

            Then("Map 컬럼의 특정 키 값을 증가시키고 새 값 리턴") {
                // 샘플 파라미터. 실제 환경에 맞춰 테이블/키/컬럼/맵키를 조정해서 사용하세요.
                val tableName = "adv-dev"
                val pk = "aaaa"
                val sk = "seq"
                val mapColumnName = "counterMap"
                val mapKey = "today"

                val newVal = aws.dynamo.updateMapSynch(
                    tableName = tableName,
                    pk = pk,
                    sk = sk,
                    columnName = mapColumnName,
                    mapKey = mapKey,
                    incrementValue = 1
                )

                log.debug { "증가 후 값: $newVal" }
            }
        }
    }
}
