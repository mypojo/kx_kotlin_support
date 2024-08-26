package net.kotlinx.domain.ddb.repeatTask

import io.kotest.matchers.shouldBe
import net.kotlinx.domain.ddb.DdbBasic
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.padStart
import java.util.*

class RepeatTaskRepositoryTest : BeSpecLight() {

    init {
        initTest(KotestUtil.PROJECT)

        DdbBasic.TABLE_NAME = "system-dev"

        Given("RepeatTask") {

            val profile = findProfile97
            val repository = RepeatTaskRepository(profile)

            When("단건 테스트") {

                val task = RepeatTask {
                    group = "job"
                    div = "demoJob"
                    memberId = "99"
                    id = UUID.randomUUID().toString()
                    time = "14:20"
                }

                Then("입력 & 조회") {

                    repository.putItem(task)

                    val ddbItem = repository.getItem(RepeatTask {
                        group = task.group
                        div = task.div
                        memberId = task.memberId
                        id = task.id
                    })!!

                    log.debug { "데이터 입력됨. id = ${ddbItem.id}" }
                    ddbItem.time shouldBe task.time
                }

                Then("삭제") {
                    repository.deleteItem(task)

                    repository.getItem(task) shouldBe null
                }

            }

            When("쿼리 테스트") {
                val groupName = "job"
                val divName = "demoJob"
                val dataset01 = (8..17).map {
                    RepeatTask {
                        group = groupName
                        div = divName
                        memberId = "11"
                        id = UUID.randomUUID().toString()
                        time = "${it.padStart(2)}:20"
                    }
                }
                val dataset02 = (2..13).map {
                    RepeatTask {
                        group = groupName
                        div = divName
                        memberId = "22"
                        id = UUID.randomUUID().toString()
                        time = "${it.padStart(2)}:20"
                    }
                }
                Then("입력") {
                    (dataset01 + dataset02).take(2).forEach {
                        repository.putItem(it)
                    }
                }

 /*               Then("계정별 조회 확인") {
                    val datas01 = repository.findBy(groupName, divName, "11")
                    println(datas01)
                }*/


                Then("삭제") {
                    (dataset01 + dataset02).forEach {
                        repository.deleteItem(it)
                    }
                }
            }


        }
    }

}
