package net.kotlinx.lock

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.delay
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import software.amazon.awssdk.utils.http.SdkHttpUtils
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.seconds

class ResourceLockManagerTest : BeSpecHeavy() {

    private val lockManager by lazy { koin<ResourceLockManager>(findProfile97()) }

    init {
        initTest(KotestUtil.FAST)

        Given("lockModule 기본테스트") {
            val lockName = "metaApiToken#987"
            val repository = lockManager.repository
            val jobs = listOf(
                "작업A" to 2,
                "작업B" to 4,
                "작업C" to 3,
            )

            Then("동시 스래드 실행 -> 락 확보") {
                jobs.map { job ->
                    Callable {
                        runBlocking {
                            SdkHttpUtils.parseNonProxyHostsEnvironmentVariable()
                            val req = ResourceLockReq {
                                resourcePk = lockName
                                lockCnt = job.second
                                div = job.first
                                cause = "테스트"
                            }
                            lockManager.acquireLock(req).use {
                                it.resources.size shouldBe job.second
                                log.debug { "${job.first} 진행중..." }
                                job.second.seconds.delay()
                                log.debug { "${job.first} 종료" }
                            }
                        }
                    }
                }.parallelExecute() //락모듈은 스래드로 실행해야함!! 코루틴 ㄴㄴ
            }

            Then("클리어") {
                val resoueces = repository.findAllByPk(lockName)
                resoueces.size shouldBe jobs.sumOf { it.second }
                resoueces.forEach { repository.deleteItem(it) }
            }
        }
    }

}
