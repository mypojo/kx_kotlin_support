package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.LakeFormationClient
import aws.sdk.kotlin.services.lakeformation.model.OptimizerType
import aws.sdk.kotlin.services.lakeformation.model.UpdateTableStorageOptimizerResponse
import aws.sdk.kotlin.services.lakeformation.updateTableStorageOptimizer


/**
 * 테이블(아이스버그)에 옵션 켜기
 * 전부 다 켬
 * => 아직 라이브러리 업데이트가 안되고있는듯?? 콘솔에서 하자
 *  */
suspend fun LakeFormationClient.updateTableAllStorageOptimizer(databaseName: String, tableName: String): UpdateTableStorageOptimizerResponse {
    return this.updateTableStorageOptimizer {
        this.databaseName = databaseName
        this.tableName = tableName
        this.storageOptimizerConfig = mapOf(
            OptimizerType.Generic to mapOf(
                "enabled" to "true",
//                "cleanupWindow" to "24h",  // 24시간 주기 클린업
//                "enableOrphanFileDeletion" to "true",
//                "retentionPeriod" to "7d"  // 7일
            ),
        )
    }
}