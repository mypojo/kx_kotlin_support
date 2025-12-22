package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.createDataSet
import aws.sdk.kotlin.services.quicksight.model.*
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.quicksight.QuicksightDataSetCreation.QuicksightDataSetConfigType


/**
 * 데이터세트 생성
 *  */
suspend fun QuickSightClient.createDataSet(dataSet: QuicksightDataSetCreation): CreateDataSetResponse = this.createDataSet {
    folderArns = dataSet.folderIds.map { "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:folder/${it}" }
    permissions = QuicksightPermissionUtil.toDataSet(awsConfig, dataSet.users)
    awsAccountId = awsConfig.awsId
    dataSetId = dataSet.dataSetId
    name = dataSet.dataSetName
    importMode = dataSet.importMode

    dataSet.rowLevelPermissionDataSet?.let { dataSetName ->
        rowLevelPermissionDataSet = RowLevelPermissionDataSet {
            permissionPolicy = RowLevelPermissionPolicy.GrantAccess //기본으로 이거
            status = Status.Enabled
            this.formatVersion = RowLevelPermissionFormatVersion.Version1  //버전2는 뭔지 모르겠음..
            this.arn = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:dataset/${dataSetName}"
        }
    }

    val physicalTableId = "AthenaTable"
    when (dataSet.type) {

        QuicksightDataSetConfigType.QUERY -> {
            physicalTableMap = mapOf(
                physicalTableId to PhysicalTable.CustomSql(
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
                    }
                )
            )
        }

        QuicksightDataSetConfigType.TABLE -> {
            physicalTableMap = mapOf(
                physicalTableId to PhysicalTable.RelationalTable(
                    RelationalTable {
                        dataSourceArn = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:datasource/${dataSet.dataSourceId}"
                        schema = dataSet.schema
                        name = dataSet.tableName
                        inputColumns = dataSet.columns.entries.map { e ->
                            InputColumn {
                                name = e.key
                                type = e.value
                            }
                        }
                    }
                )
            )
        }
    }

    logicalTableMap = mapOf(
        "LogicalTable-01" to LogicalTable {
            alias = dataSet.dataSetName
            source = LogicalTableSource {
                this.physicalTableId = physicalTableId
            }
        }
    )
}