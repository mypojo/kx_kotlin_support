package net.kotlinx.lock

/**
 * 리소스 생성기
 */
interface ResourceItemFactory {


    /**
     * @return 요청된 숫자보다 적게 리턴될 수 있음!!
     * */
    suspend fun createResource(lockReq: ResourceLockReq, cnt: Int): List<ResourceItem>


}