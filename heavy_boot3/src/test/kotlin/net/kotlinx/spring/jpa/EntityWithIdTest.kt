package net.kotlinx.spring.jpa

import io.kotest.matchers.shouldBe
import net.kotlinx.domain.jpa.EntityWithId
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class EntityWithIdTest : BeSpecLight() {


    init {
        initTest(KotestUtil.FAST)

        Given("Entity 간단검증") {

            class Poo : EntityWithId<Long>() {
                var pooId: Long? = null
                var name: String? = null
                override fun getId(): Long = pooId!!
            }

            Then("기본구현 확인") {

                val poo = Poo()
                poo.pooId = 123L

                poo.isNew shouldBe false
                poo.isNew = true
                poo.isNew shouldBe true

                poo.id shouldBe poo.pooId

            }
        }
    }

}
