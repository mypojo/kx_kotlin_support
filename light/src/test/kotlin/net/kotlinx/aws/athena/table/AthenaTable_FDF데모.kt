package net.kotlinx.aws.athena.table

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class AthenaTable_FDF데모 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("데모 테이블") {

            Then("demo") {
                val table = AthenaTable {
                    icebugTable()
                    database = "d"
                    bucket = "xx-work-dev"
                    tableName = "demo"
                    s3Key = "data/${tableName}/"
                    schema = mapOf(
                        "basic_date" PARTITION string,
                        "work_type" PARTITION string,
                        "name" NK string OPTION {
                            //옵션 추가
                        },
                        "cost" to int,
                        "detail" AS mapOf(
                            "eventId" to bigint,
                            "eventDate" to string,
                            "datas" AS listOf(
                                "source" to string,
                                "g1" to string,
                                "g2" to string,
                            ),
                        )
                    )
                }
                println(table.create())
            }
        }

    }

}
