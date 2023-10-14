package net.kotlinx.aws.athena

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.io.path.deleteExisting

internal class AthenaModuleTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `기본테스트`() {
        val executions = listOf(
            AthenaReadAll(
                """
                    SELECT basic_date "날짜",kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H') "시간",COUNT(1) CNT 
                    FROM autobid_rank
                    group by  basic_date,kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H')
                    order by basic_date,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H')
                """
            ) { lines ->
                println(lines.size)
            },
            AthenaDownload(
                """
                    SELECT basic_date "날짜",kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H') "시간",COUNT(1)+1 CNT 
                    FROM autobid_rank
                    group by  basic_date,kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H')
                    order by basic_date,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H')
                """
            ) { file ->
                println("파일 다운로드 : ${file.absolutePath}")
                csvReader().open(file) {
                    readAllAsSequence().forEach {
                        println(it)
                    }
                }
                file.toPath().deleteExisting()
            },
        )
        val athenaModule = AthenaModule(aws, workGroup = "workgroup-prod", database = "p")
        athenaModule.startAndWaitAndExecute(executions)

    }


}