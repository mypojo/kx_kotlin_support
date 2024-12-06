package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.deleteAnalysis
import aws.sdk.kotlin.services.quicksight.listAnalyses
import aws.sdk.kotlin.services.quicksight.model.AnalysisSummary
import aws.sdk.kotlin.services.quicksight.model.DeleteAnalysisResponse
import net.kotlinx.aws.awsConfig
import net.kotlinx.string.toTextGridPrint

/** 간단 출력 */
fun List<AnalysisSummary>.printSimple() {
    listOf("id", "status", "createdTime", "name", "lastUpdatedTime").toTextGridPrint {
        this.map {
            arrayOf(it.analysisId, it.name, it.status, it.createdTime, it.lastUpdatedTime)
        }
    }
}

/** 분석 리스팅 */
suspend fun QuickSightClient.listAnalyses(): List<AnalysisSummary> {
    val resp = this.listAnalyses {
        awsAccountId = awsConfig.awsId
    }
    return resp.analysisSummaryList!!
}


/** 분석 삭제 */
suspend fun QuickSightClient.deleteAnalysis(analysisId: String): DeleteAnalysisResponse = this.deleteAnalysis {
    this.awsAccountId = awsConfig.awsId
    this.analysisId = analysisId
}