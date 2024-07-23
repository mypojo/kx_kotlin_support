package net.kotlinx.aws.javaSdkv2.dynamoLock

import io.kotest.assertions.throwables.shouldThrow
import net.kotlinx.concurrent.sleep
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import kotlin.time.Duration.Companion.seconds

class DynamoLockManagerTest : BeSpecHeavy() {

    private val lockModule by lazy { koin<DynamoLockManager>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("DynamoLockModule") {

            Then("락 테스트") {

                val req01 = DynamoLockReq {
                    pk = "aa"
                    sk = "bb"
                    div = "for test"
                    comment = "과금 테스트 입니다"
                }

                lockModule.acquireLock(req01).use {
                    log.info { "첫번째 락을 잡습니다..." }
                    1.seconds.sleep()

                    val req02 = DynamoLockReq {
                        pk = "aa"
                        sk = "cc"
                        div = "for test v2"
                        comment = "과금 테스트 입니다 v2"
                    }
                    lockModule.acquireLock(req02).use {
                        log.info { "두번째 락(다른값)을 잡습니다..." }
                        2.seconds.sleep()
                        log.info { "두번째 락 종료" }
                    }

                    shouldThrow<DynamoLockManager.DynamoLockFailException> {
                        val req03 = DynamoLockReq {
                            pk = "aa"
                            sk = "bb" //첫 락과 동일값
                            div = "for test v3"
                            comment = "과금 테스트 입니다 v3"
                        }
                        lockModule.acquireLock(req03).use {
                            log.info { "세번째 락(같은값)을 잡습니다" }
                            throw IllegalStateException("이 락이 통과되면 안됨!!")
                        }
                    }

                    log.info { "첫번째 락 종료" }
                }
            }
        }
    }


}