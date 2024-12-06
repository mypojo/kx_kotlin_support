package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.*
import aws.sdk.kotlin.services.quicksight.model.*
import net.kotlinx.aws.awsConfig
import net.kotlinx.collection.doUntilTimeout
import net.kotlinx.string.toTextGridPrint
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** 간단 출력 */
fun List<DataSetSummary>.printSimple() {
    listOf("id", "importMode", "name", "createdTime", "lastUpdatedTime").toTextGridPrint {
        this.map {
            arrayOf(it.dataSetId, it.importMode, it.name, it.createdTime, it.lastUpdatedTime)
        }
    }
}

/**
 * 데이터세트 리스팅
 * UI에는 안나오는게 간혹 있음..
 *  */
suspend fun QuickSightClient.listDataSets(): List<DataSetSummary> {
    val resp = this.listDataSets {
        awsAccountId = awsConfig.awsId
    }
    return resp.dataSetSummaries!!
}

/** 리스팅은 상세정보를 안줌 */
suspend fun QuickSightClient.describeDataSet(dataSetId: String): DataSet {
    return this.describeDataSet {
        this.awsAccountId = awsConfig.awsId
        this.dataSetId = dataSetId
    }.dataSet!!
}

/** 데이터세트 삭제 */
suspend fun QuickSightClient.deleteDataSet(dataSetId: String): DeleteDataSetResponse = this.deleteDataSet {
    this.awsAccountId = awsConfig.awsId
    this.dataSetId = dataSetId
}

/** 데이터 새로고침 */
suspend fun QuickSightClient.createIngestion(dataSetId: String): CreateIngestionResponse = this.createIngestion {
    this.awsAccountId = awsConfig.awsId
    this.dataSetId = dataSetId
    this.ingestionId = UUID.randomUUID().toString() //유니크 ID 생성해서 넣어줌
    this.ingestionType = IngestionType.FullRefresh
}

/** 데이터 새로고침 체크 */
suspend fun QuickSightClient.describeIngestion(dataSetId: String, ingestionId: String): DescribeIngestionResponse = this.describeIngestion {
    this.awsAccountId = awsConfig.awsId
    this.dataSetId = dataSetId
    this.ingestionId = ingestionId
}

/**
 * 데이터세트 리프레시
 * SPICE 적용된 순서대로 갱신 해줘야함 -> 최종 데이터셋이 갱신되면 대시보드도 자동으로 변경됨
 *  */
suspend fun QuickSightClient.refreshDataSet(dataSetId: String, synch: Boolean = false, checkInterval: Duration = 10.seconds, checkTimeout: Duration = 10.minutes): IngestionStatus {
    val ingestion = createIngestion(dataSetId)
    if (!synch) return IngestionStatus.Queued

    return doUntilTimeout(checkInterval, checkTimeout) {
        val describeIngestion = describeIngestion(dataSetId, ingestion.ingestionId!!)
        val status = describeIngestion.ingestion!!.ingestionStatus
        if (status in setOf(IngestionStatus.Completed, IngestionStatus.Failed, IngestionStatus.Cancelled)) status else null
    }
}

/** 데이터세트 생성 */
suspend fun QuickSightClient.createDataSet(dataSet: QuicksightDataSetConfig): CreateDataSetResponse = this.createDataSet {
    folderArns = dataSet.folderIds.map { "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:folder/${it}" }
    permissions = QuicksightPermissionUtil.toDataSet(awsConfig, dataSet.users)
    awsAccountId = awsConfig.awsId
    dataSetId = dataSet.dataSetId
    name = dataSet.dataSetName
    importMode = dataSet.importMode
    physicalTableMap = mapOf(
        "AthenaTable" to PhysicalTable.CustomSql(
            CustomSql {
                dataSourceArn = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:datasource/${dataSet.dataSourceId}"
                name = dataSet.dataSetId //일단 동일하게
                sqlQuery = dataSet.query
                columns = dataSet.columns.entries.map { e ->
                    InputColumn {
                        name = e.key
                        type = e.value
                    }
                }
            })
    )

}


//suspend fun QuickSightClient.asd(dataSetId: String, ingestionId: String) {
//
//
//    this.startAssetBundleExportJob {
//        this.awsAccountId = awsConfig.awsId
//        this.dataSetId = dataSetId
//        this.exportJobId = UUID.randomUUID().toString()
//        this.exportJobType = ExportJobType.AssetBundle
//        this.exportJobName = "exportJobName"
//        this.exportJobOutput = ExportJobOutput{
//            this.assetBundleExportJobOutput = AssetBundleExportJobOutput{
//                this.assetBundleExportJobOutputType = AssetBundleExportJobOutputType.AssetBundle
//                this.assetBundleExportJobOutputLocation = AssetBundleExportJobOutputLocation{
//                    this.s3ExportLocation = S3ExportLocation{
//                        this.bucket = "XXXXXX"
//                        this.prefix = "prefix"
//                    }
//                }
//            }
//        }
//    }
//}