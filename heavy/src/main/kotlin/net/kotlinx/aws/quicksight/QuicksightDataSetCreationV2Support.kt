package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.createDataSet
import aws.sdk.kotlin.services.quicksight.model.*
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.quicksight.QuicksightDataSetCreation.QuicksightDataSetConfigType

/**
 * 데이터세트 생성 (New Data Preparation Experience)
 */
suspend fun QuickSightClient.createDataSetV2(dataSet: QuicksightDataSetCreation): CreateDataSetResponse = this.createDataSet {
    folderArns = dataSet.folderIds.map { "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:folder/${it}" }
    permissions = QuicksightPermissionUtil.toDataSet(awsConfig, dataSet.users)
    awsAccountId = awsConfig.awsId
    dataSetId = dataSet.dataSetId
    name = dataSet.dataSetName
    importMode = dataSet.importMode

    val physicalTableId = "AthenaTable"
    val sourceTableId = "source-table-01"
    val importStepId = "import-step-01"
    val destinationTableId = "destination-table-01"

    // Physical Table 정의
    when (dataSet.type) {
        QuicksightDataSetConfigType.QUERY -> {
            physicalTableMap = mapOf(
                physicalTableId to PhysicalTable.CustomSql(
                    CustomSql {
                        dataSourceArn = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:datasource/${dataSet.dataSourceId}"
                        name = dataSet.dataSetId
                        sqlQuery = dataSet.query
                        columns = dataSet.columns.entries.map { e ->
                            InputColumn {
                                id = e.key
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
                                id = e.key
                                name = e.key
                                type = e.value
                            }
                        }
                    }
                )
            )
        }
    }

    // New Data Preparation Configuration.  소스 -> 변환 - > 대상  이렇게 3단계
    dataPrepConfiguration = DataPrepConfiguration {
        sourceTableMap = mapOf(
            sourceTableId to SourceTable {
                this.physicalTableId = physicalTableId
            }
        )
        transformStepMap = mapOf(
            importStepId to TransformStep {
                importTableStep = ImportTableOperation {
                    alias = "athena-load"  //최초 로드시 보이는 그 이름
                    source = ImportTableOperationSource {
                        this.sourceTableId = sourceTableId
                    }
                }
            }
        )
        destinationTableMap = mapOf(
            destinationTableId to DestinationTable {
                alias = dataSet.dataSetName
                source = DestinationTableSource {
                    transformOperationId = importStepId
                }
            }
        )
    }

    // Semantic Model Configuration (RLS 설정 포함)
    semanticModelConfiguration = SemanticModelConfiguration {
        tableMap = mapOf(
            destinationTableId to SemanticTable {
                alias = dataSet.dataSetName
                this.destinationTableId = destinationTableId

                // Row Level Permission 설정
                dataSet.rowLevelPermissionDataSet?.let { dataSetName ->
                    rowLevelPermissionConfiguration = RowLevelPermissionConfiguration {
                        rowLevelPermissionDataSet = RowLevelPermissionDataSet {
                            permissionPolicy = RowLevelPermissionPolicy.GrantAccess
                            status = Status.Enabled
                            formatVersion = RowLevelPermissionFormatVersion.Version1
                            arn = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:dataset/${dataSetName}"
                        }
                    }
                }
            }
        )
    }
}