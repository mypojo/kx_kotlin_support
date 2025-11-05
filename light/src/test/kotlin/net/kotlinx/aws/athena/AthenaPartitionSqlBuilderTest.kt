package net.kotlinx.aws.athena

import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.TimeUtil
import net.kotlinx.time.toTimeString
import net.kotlinx.time.toYmd
import java.time.LocalDate

internal class AthenaPartitionSqlBuilderTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("AthenaModule") {

            val athenaModule by koinLazy<AthenaModule>()

            Then("파티션_LIMIT") {
                val start = System.currentTimeMillis()
                val builder = AthenaPartitionSqlBuilder {
                    bucketName = "sin-data-dev"
                    prefix = "data"
                }
                val basicDates = listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().plusDays(1)).map { it.toYmd() }

                val datas = basicDates.flatMap { basicDate ->
                    (0 until 2500).map {
                        linkedMapOf("basic_date" to basicDate, "kwd_id" to it.toString().padStart(3, '0'))
                    }
                }
                log.info { "파티션 수 ${datas.size}" }
                //val addSql = builder.generateAddSql("d.demo", datas)
                val addSqls = builder.generateAddSqlBatch("d.demo", datas)
                //log.info { "파티션 addSql \n$addSql" }

                athenaModule.startAndWaitAndExecute(addSqls.map { AthenaExecute(it) })
                log.info { "파티션 작업 종료 : 걸린시간 ${(System.currentTimeMillis() - start).toTimeString()}" }
            }

            Then("파티션_생성") {
                val builder = AthenaPartitionSqlBuilder {
                    bucketName = "sin-autobid"
                    prefix = "data"
                }

                //전후 하루씩 추가
                val basicDates = listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().plusDays(1)).map { it.toYmd() }
                val kwds = listOf("청바지", "반바지")

                val datas = basicDates.flatMap { basicDate ->
                    kwds.map {
                        linkedMapOf("basic_date" to basicDate, "kwd_id" to it)
                    }
                }
                val addSql = builder.generateAddSql("nb.autobid_rank", datas)
                println(addSql)

                athenaModule.execute(addSql)
            }

            Then("파티션_생성 v2") {
                val builder = AthenaPartitionSqlBuilder {
                    bucketName = "md2-db"
                    prefix = "data1"
                }

                //전후 하루씩 추가
                val basicDates = TimeUtil.between("20251110" to "20271231")
                val advIds = listOf("302565")

                val datas = basicDates.flatMap { basicDate ->
                    advIds.map {
                        linkedMapOf("basic_date" to basicDate, "adv_id" to it)
                    }
                }
                val addSql = builder.generateAddSql("adtrigger.kk_master_kwd", datas)
                println(addSql)

                //athenaModule.execute(addSql)
            }
        }
    }

}