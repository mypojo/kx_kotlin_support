package net.kotlinx.aws.kinesis

import net.kotlinx.aws.firehose.logData.LogDataTable
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class KinesisSupportKt_log_data_테이블생성 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본 입력/조회/삭제") {

            Then("스키마 확인") {
                val table = LogDataTable.LOG_DATA.apply {
                    database = "ds"
                    bucket = "dmp-work-dev"
                    s3Key = "ds/${tableName}/"
                }
                println(table.create())

            }
        }
    }

}
