package net.kotlinx.aws.athena

import net.kotlinx.TestRoot
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core1.time.toYmd
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AthenaPartitionSqlBuilderTest : TestRoot() {

    @Test
    fun `파티션_생성`() {


        val builder = AthenaPartitionSqlBuilder("sin-autobid", "data")

        //전후 하루씩 추가
        val basicDates = listOf(LocalDate.now().minusDays(1), LocalDate.now(), LocalDate.now().plusDays(1)).map { it.toYmd() }
        val kwds = listOf("청바지", "반바지")

        val datas = basicDates.flatMap { basicDate ->
            kwds.map {
                linkedMapOf("basic_date" to basicDate, "kwd_name" to it)
            }
        }
        val addSql = builder.generateAddSql("nb.autobid_rank", datas)
        println(addSql)

        val aws = AwsConfig(profileName = "sin").toAwsClient()

        val athenaModule = AthenaModule(aws)
        athenaModule.execute(addSql)

        //athenaModule.runAthenaExecution(addSql)


    }
}