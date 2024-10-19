package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.ExportFormat
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.athena.AthenaTable
import net.kotlinx.aws.athena.AthenaTableFormat
import net.kotlinx.aws.athena.AthenaTablePartitionType
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class DynamoDbExporterKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("DynamoDbExporter") {

            val aws by koinLazy<AwsClient>()

            Then("step1 - S3로 익스포트") {
                val exporter = DynamoExporter(aws) {
                    tableName = "job-dev"
                    s3Bucket = "sin-work-dev"
                    s3Prefix = "temp/ddb-export/job-dev/"
                    exportFormat = ExportFormat.Ion
                }
                exporter.exportAndWait()
                println(exporter.s3ResultPath)
            }

            Then("step2 - ion 형식으로 external 테이블 생성") {
                val basicDate = "20220101"
                val table = AthenaTable().apply {
                    tableName = "job_dev_$basicDate"
                    bucket = "sin-work-dev"
                    s3Key = "temp/ddb-export/job-dev/..."  //s3ResultPath
                    athenaTableFormat = AthenaTableFormat.IonDdb
                    athenaTablePartitionType = AthenaTablePartitionType.NONE
                    ionFlatPath = "Item"
                    schema = mapOf(
                        "pk" to string,
                        "sk" to string,
                        "instance_type" to string,
                        "member_id" to string,
                        "expire_time" to string,
                    )
                }
                log.info { "테이블 [${table.tableName}] 생성 \n${table.create()}" }

                val athenaModule = AthenaModule {
                    workGroup = "workgroup-dev"
                    database = "wd"
                }
                athenaModule.execute(table.drop())
                athenaModule.execute(table.create())
                //최종 업무 테이블은 view 테이블 변환해서 사용 & X일치 테이블 드랍
                athenaModule.execute("create or replace view job_dev as select * from ${table.tableName}")
            }

            Then("step3 - 데이터 읽기??") {

            }
        }
    }
}