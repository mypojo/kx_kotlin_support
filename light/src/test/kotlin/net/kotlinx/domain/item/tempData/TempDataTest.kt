package net.kotlinx.domain.item.tempData

import com.lectra.koson.obj
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print
import kotlin.time.Duration.Companion.minutes

class TempDataTest : BeSpecLight() {


    init {
        initTest(KotestUtil.PROJECT)

        Given("TempData") {

            val tempData = TempData("xxJob", "59110001").apply {
                body = obj {
                    "kwd" to "청바지"
                }.toString()
                ttl = DynamoUtil.ttlFromNow(5.minutes)
            }

            val rep = TempDataRepository(findProfile97)

            Then("데이터 체크") {
                rep.getItem(tempData) shouldBe null
            }

            Then("데이터 입력 & 체크") {
                rep.putItem(tempData)
                val exist = rep.getItem(tempData)
                listOf(tempData, exist).map { GsonData.fromObj(it) }.print()
                exist shouldBe tempData
            }

            Then("데이터 삭제") {
                rep.deleteItem(tempData)
                rep.getItem(tempData) shouldBe null
            }

        }


    }


}
