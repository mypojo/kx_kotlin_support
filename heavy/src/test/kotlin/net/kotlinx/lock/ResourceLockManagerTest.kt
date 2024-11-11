package net.kotlinx.lock

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.delay
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.seconds

class ResourceLockManagerTest : BeSpecHeavy() {

    private val lockManager by lazy { koin<ResourceLockManager>(findProfile97) }

    init {
        initTest(KotestUtil.FAST)

        Given("lockModule 기본테스트") {
            val lockName = "metaApiToken#987"
            log.trace { "system 테이블에 ${lockName}/일련번호 로 락이 생김" }

            val repository = lockManager.repository

            /** 시간을 늘리면  DDB 테이블 데이터 쉽게 확인 가능 */
            val jobs = listOf(
                "작업A" to 2,
                "작업B" to 4,
                "작업C" to 3,
            )
            log.trace { "${jobs.size}개의 잡이 각각 x개의 리소스를 요청함" }

            Then("동시 스래드 실행 -> 락 확보") {
                jobs.map { job ->
                    Callable {
                        runBlocking {
                            val req = ResourceLockReq {
                                resourcePk = lockName
                                lockCnt = job.second
                                div = job.first
                                cause = "테스트"
                            }
                            lockManager.acquireLock(req).use {
                                it.resources.size shouldBe job.second
                                log.debug { "${job.first} 진행중...  ${job.second.seconds}초 딜레이.." }
                                job.second.seconds.delay()
                                log.debug { "${job.first} 종료" }
                            }
                        }
                    }
                }.parallelExecute() //락모듈은 스래드로 실행해야함!! 코루틴 ㄴㄴ

                log.warn { "리소스 테이블에 리소스가 X개 있어야함" }
            }

            Then("추가 작업 실행 -> 기존 리소스 재사용") {
                val req = ResourceLockReq {
                    resourcePk = lockName
                    lockCnt = 4
                    div = "추가작업"
                    cause = "테스트"
                }
                lockManager.acquireLock(req).use {
                    1.seconds.delay()
                }
            }

            Then("리소스 숫자 체크") {
                val resoueces = repository.findAllByPk(lockName)
                log.trace { "추가 작업을 했더라도 리소스가 더 늘지는 않음" }
                resoueces.size shouldBe jobs.sumOf { it.second }
            }

            Then("클리어") {
                val resoueces = repository.findAllByPk(lockName)
                resoueces.forEach { repository.deleteItem(it) }
            }
        }
    }

}
