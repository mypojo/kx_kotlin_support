package net.kotlinx.lock

import io.kotest.matchers.shouldBe
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

class ResourceItemRepositoryTest : BeSpecHeavy() {

    private val lock by lazy { koin<ResourceLockManager>(findProfile97) }


    init {
        initTest(KotestUtil.PROJECT)

        Given("ResourceItemRepository 기본테스트") {

            val pk1 = "ResourceItemRepositoryTest#1"
            val pk2 = "ResourceItemRepositoryTest#2"
            val repository = lock.repository
            Then("입력 -> 리소스 테이블에 데이터가 생김") {
                val req1 = ResourceLockReq {
                    resourcePk = pk1
                    cause = "강제테스트" //이 문구는 락에 대한 정보라서, 리소스에는 저장 안됨
                }
                val req2 = ResourceLockReq {
                    resourcePk = pk2
                    cause = "두반째 테스트"
                }

                lock.factory.createResource(req1, 3).forEach { repository.putItem(it) }
                lock.factory.createResource(req2, 2).forEach { repository.putItem(it) }
            }

            Then("리스팅 & 업데이트") {
                val items = repository.findAllByPk(pk1)
                items.print()
                items.size shouldBe 3
                repository.updateItemInUse(items.take(2), true)
                repository.findAllByPk(pk1).filter { it.inUse }.size shouldBe 2
            }


            Then("클리어") {
                repository.findAllByPk(pk1).forEach { repository.deleteItem(it) }
                repository.findAllByPk(pk2).forEach { repository.deleteItem(it) }
            }

        }
    }

}
