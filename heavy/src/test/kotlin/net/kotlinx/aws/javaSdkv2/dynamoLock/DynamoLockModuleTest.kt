package net.kotlinx.aws.javaSdkv2.dynamoLock

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.dynamo.getItem
import net.kotlinx.aws.javaSdkv2.AwsJavaSdkV2Client
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.concurrent.sleep
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class DynamoLockModuleTest : TestRoot() {

    val awsConfig = AwsConfig(profileName = "sin")


    @Test
    fun test() {

        val aws = awsConfig.toAwsClient()

        val lockModule = DynamoLockModule(AwsJavaSdkV2Client(awsConfig).ddb) {
            tableName = "dist_lock-dev"
            leaseDuration = 2
            heartbeatPeriod = 1
            defaultAdditionalTimeout = 5
        }

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
                    val existLock = aws.dynamo.getItem(e.req)!!
                    log.warn { " 지금 선행되어있는락 때문에 실패!! ->  $existLock" }
                }
            }
            log.info { "첫번째 락 종료" }
        }

    }

}