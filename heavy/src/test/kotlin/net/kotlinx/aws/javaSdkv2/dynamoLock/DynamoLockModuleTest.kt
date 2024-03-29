package net.kotlinx.aws.javaSdkv2.dynamoLock

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import net.kotlinx.core.concurrent.sleep
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class DynamoLockModuleTest : TestLight() {

    val lockModule = DynamoLockModule {
        tableName = "dist_lock-dev"
        leaseDuration = 2
        heartbeatPeriod = 1
        defaultAdditionalTimeout = 5
    }

    @Test
    fun test() {

        val req01 = DynamoLockReq {
            pk = "aa"
            sk = "bb"
            div = "for test"
            comment = "과금 테스트 입니다"
            tableName = lockModule.tableName
        }

        lockModule.acquireLock(req01).use {
            log.info { "첫번째 락을 잡습니다..." }
            3.seconds.sleep()

            lockModule.acquireLock(DynamoLockReq {
                pk = "aa"
                sk = "cc"
                div = "for test v2"
                comment = "과금 테스트 입니다 v2"
                tableName = lockModule.tableName
            }).use {
                log.info { "두번째 락을 잡습니다..." }
                5.seconds.sleep()
                log.info { "두번째 락 종료" }
            }

            try {
                lockModule.acquireLock(DynamoLockReq {
                    pk = "aa"
                    sk = "bb" //첫 락과 동일값
                    div = "for test v2"
                    comment = "과금 테스트 입니다 v2"
                    tableName = lockModule.tableName
                }).use {
                    log.info { "세번째 락(동일)을 잡습니다" }
                    throw IllegalStateException("이 락이 통과되면 안됨!!")
                }
            } catch (e: DynamoLockModule.DynamoLockFailException) {
                runBlocking {
                    val aws: AwsClient1 by inject()
                    val req = e.req
                    //val existLock = aws.dynamo.getItem(req.pk,e.req.sk)!!
                    //log.warn { " 지금 선행되어있는락 때문에 실패!! ->  $existLock" }
                }
            }
            "".isNullOrEmpty()
            log.info { "첫번째 락 종료" }
        }

    }

}