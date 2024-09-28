package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.deleteDataSet
import aws.sdk.kotlin.services.quicksight.describeDataSet
import aws.sdk.kotlin.services.quicksight.listDataSets
import aws.sdk.kotlin.services.quicksight.model.DataSet
import aws.sdk.kotlin.services.quicksight.model.DataSetSummary
import aws.sdk.kotlin.services.quicksight.model.DeleteDataSetResponse
import net.kotlinx.aws.awsConfig
import net.kotlinx.string.toTextGridPrint

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

suspend fun QuickSightClient.deleteDataSet(dataSetId: String): DeleteDataSetResponse = this.deleteDataSet {
    this.awsAccountId = awsConfig.awsId
    this.dataSetId = dataSetId
}