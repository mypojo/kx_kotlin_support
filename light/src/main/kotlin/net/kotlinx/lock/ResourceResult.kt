package net.kotlinx.lock

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

/**
 * 락 잡힌 리소스 결과세트
 *  */
class ResourceResult(
    private val repository: ResourceItemRepository,
    val resources: List<ResourceItem>,
) : AutoCloseable {

    override fun close() {
        log.trace { "DDB에 사용완료로 업데이트" }
        runBlocking { repository.updateItemInUse(resources, false) }
    }


    companion object {
        private val log = KotlinLogging.logger {}
    }

}