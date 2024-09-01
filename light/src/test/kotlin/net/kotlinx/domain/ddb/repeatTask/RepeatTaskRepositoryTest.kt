package net.kotlinx.domain.ddb.repeatTask

import io.kotest.matchers.shouldBe
import net.kotlinx.domain.ddb.DdbBasic
import net.kotlinx.domain.ddb.DdbBasicGsi
import net.kotlinx.domain.ddb.DdbBasicRepository
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.padStart
import net.kotlinx.string.print
import java.util.*

class RepeatTaskRepositoryTest : BeSpecLight() {

    val repository by lazy { DdbBasicRepository(findProfile97, RepeatTaskConverter()) }

    init {
        initTest(KotestUtil.PROJECT)

        DdbBasic.TABLE_NAME = "system-dev"

        Given("RepeatTask") {

            When("단건 테스트") {

                val task = RepeatTask {
                    group = "job"
                    div = "demoJob"
                    memberId = "99"
                    id = UUID.randomUUID().toString()
                    time = "14:20"
                    body = GsonData.obj {
                        put("aa", 123)
                        put("bb", "xx")
                    }
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
                    ddbItem.body["bb"].str shouldBe "xx"
                }

                Then("삭제") {
                    repository.deleteItem(task)
                    repository.getItem(task) shouldBe null
                }
            }

        }

        Given("쿼리 테스트") {
            When("다수 데이터 입력 후 조회 & 삭제") {
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
                    (dataset01 + dataset02).forEach {
                        repository.putItem(it)
                    }
                }

                When("계정단위 조회") {

                    Then("계정 11") {
                        val req = RepeatTask {
                            group = groupName
                            div = divName
                            memberId = "11"
                        }
                        val datas = repository.findBySkPrefix(req)
                        datas.datas.size shouldBe 10
                    }

                    Then("계정 22 (리미트걸어도 여러번 호출해서 전체 가져옴)") {
                        val req = RepeatTask {
                            group = groupName
                            div = divName
                            memberId = "22"
                        }
                        val datas = repository.findAllBySkPrefix(req) {
                            limit = 5
                        }
                        datas.size shouldBe 12
                    }

                }

                When("GSI01 조회 (시간단위)") {

                    Then("09시20분 2개 조회됨. 11꺼 하나, 22꺼 하나") {
                        val currentTime = "09:20"
                        val req = RepeatTask {
                            time = currentTime
                        }
                        val datas = repository.findBySkPrefix(DdbBasicGsi.GSI01, req)
                        datas.datas.print()
                        datas.datas.size shouldBe 2

                        datas.datas.first { it.memberId == "11" && it.time == currentTime }.id shouldBe dataset01.first { it.time == currentTime }.id
                        datas.datas.first { it.memberId == "22" && it.time == currentTime }.id shouldBe dataset02.first { it.time == currentTime }.id
                    }

                }

                Then("삭제") {
                    (dataset01 + dataset02).forEach {
                        repository.deleteItem(it)
                    }
                }
            }
        }

    }

}
