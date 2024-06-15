package net.kotlinx.lock

import mu.KotlinLogging
import net.kotlinx.aws.javaSdkv2.dynamoLock.DynamoLockManager
import net.kotlinx.aws.javaSdkv2.dynamoLock.DynamoLockReq
import net.kotlinx.core.Kdsl
import net.kotlinx.reflect.name

/**
 * https://www.notion.so/mypojo/ec1e756eada4484d9be90aba2f2c574a
 *
 * 1. 다수의 동시 리소스를 제한하는 매니저
 * 2. 리소스 생성 수 입력하면 리소스를 생성해주고 closable 을 리턴
 *
 * 성능상의 이슈로 최초 생성시 락은 잡지만 closable 로직에서는 락을 잡지 않음
 *
 * 주의!!! 스래드로 실행하세요!! 코루틴 ㄴㄴ
 * */
class ResourceLockManager {

    @Kdsl
    constructor(block: ResourceLockManager.() -> Unit = {}) {
        apply(block)
    }

    /** 락 모듈 */
    lateinit var lockManager: DynamoLockManager

    /** 락 PK */
    var lockPk = this::class.name()

    /** rep */
    lateinit var repository: ResourceItemRepository

    /**
     * 리소스를 생성하는 factory
     * 이 factory 내부의 작업은 모두 lockPk 단위로 동기화되어 실행됨으로 안전하다.
     * */
    lateinit var factory: ResourceItemFactory

    /**
     * 리소스 부족시 요청된 숫자보다 적게 리턴될 수 있음!! 받아서 어떻게 할지 각자판단
     * */
    suspend fun acquireLock(req: ResourceLockReq): ResourceResult {

        check(req.lockCnt > 0)

        log.trace { "일단 락을 잡는다." }
        val lockReq = DynamoLockReq {
            pk = lockPk
            sk = req.resourcePk
            div = req.div
            comment = req.cause
        }

        return lockManager.acquireLock(lockReq).use {

            val items = run {
                log.trace { "리소스 전체 조회" }

                val currentSec = System.currentTimeMillis() / 1000
                val partition = repository.findAllByPk(req.resourcePk).partition { it.ttl < currentSec }
                if (partition.first.isNotEmpty()) {
                    log.warn { "TTL 지난 ${partition.first.size}건 강제 삭제 (그냥 놔둬도 삭제되긴 함). 현시간초 $currentSec" }
                    partition.first.onEach { repository.deleteItem(it) }
                }

                val allItems = partition.second
                val validItems = allItems.filter { !it.inUse }
                log.debug { "리소스(미사용중/전체) 현황 : ${validItems.size}/${allItems.size}" }
                validItems
            }

            log.trace { "전달할 리소스를 확보" }
            val resourceItems = run {
                val existResources = items.take(req.lockCnt)
                val createCnt = req.lockCnt - existResources.size
                check(createCnt >= 0)
                log.trace { "부족한 리소스는 factory에서 추가" }
                val newResources = if (createCnt == 0) emptyList() else factory.createResource(req, createCnt)
                newResources.forEach { repository.putItem(it) }
                existResources + newResources
            }

            log.trace { "DDB에 사용중으로 업데이트" }
            resourceItems.forEach {
                it.div = req.div
                it.cause = req.cause
            }
            repository.updateItemInUse(resourceItems, true)
            ResourceResult(repository, resourceItems)
        }
    }


    companion object {
        private val log = KotlinLogging.logger {}
    }


}