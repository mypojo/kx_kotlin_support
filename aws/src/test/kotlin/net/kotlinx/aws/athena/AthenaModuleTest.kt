package net.kotlinx.aws.athena

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.kotlinx.TestRoot
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import org.junit.jupiter.api.Test
import kotlin.io.path.deleteExisting

internal class AthenaModuleTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `기본테스트`() {
        val executions = listOf(
            AthenaReadAll(
                """
                    SELECT basic_date "날짜",kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H') "시간",COUNT(1)+1 CNT 
                    FROM nb.autobid_rank
                    group by  basic_date,kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H')
                    order by basic_date,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H')
                """
            ) { lines ->
                lines.forEach { println(it) }
            },
            AthenaDownload(
                """
                    SELECT basic_date "날짜",kwd_name,date_format(event_time AT TIME ZONE 'Asia/Seoul', '%Y-%m-%d %H') "시간",COUNT(1)+1 CNT 
                    FROM nb.autobid_rank
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
        val athenaModule = AthenaModule(aws, workGroup = "workgroup-dev")
        athenaModule.startAndWaitAndExecute(executions)

    }


}