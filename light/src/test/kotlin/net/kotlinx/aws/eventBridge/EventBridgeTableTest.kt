package net.kotlinx.aws.eventBridge

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class EventBridgeTableTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("EventBridgeTable") {

            Then("테이블 생성 dev") {
                val table = EventBridgeTable.EVENTBRIDGE_LOG.apply {
                    database = "d1"
                    bucket = "xx-work-dev"
                    s3Key = "data/level1/${tableName}/"
                }
                athenaModule97.execute(table.dropForce())
                athenaModule97.execute(table.create())
            }

            Then("테이블 생성 prod") {
                val table = EventBridgeTable.EVENTBRIDGE_LOG.apply {
                    database = "p1"
                    bucket = "xx-work-prod"
                    s3Key = "data/level1/${tableName}/"
                }
                athenaModule97.execute(table.dropForce())
                athenaModule97.execute(table.create())
            }

        }
    }
}

