package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.createDataSource
import aws.sdk.kotlin.services.quicksight.deleteDataSource
import aws.sdk.kotlin.services.quicksight.listDataSources
import aws.sdk.kotlin.services.quicksight.model.*
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.toKoreaDateTime
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01

/** 간단 출력 */
fun List<DataSource>.printSimple() {
    listOf("id", "type", "name", "arn", "createdTime", "lastUpdatedTime").toTextGridPrint {
        this.map {
            arrayOf(
                it.dataSourceId,
                it.type,
                it.name,
                it.arn,
                it.createdTime!!.toKoreaDateTime().toKr01(),
                it.lastUpdatedTime!!.toKoreaDateTime().toKr01()
            )
        }
    }
}

/**
 * 데이터소스 리스팅
 * 데이터소스는 데이터의 출처만 정의하고 실제 데이터를 저장하지 않는다.
 * 데이터 세트가 만들어질때 실제 데이터가 로드되고 SPICE 등의 용량을 차지한다.
 *
 * 콘솔에 이걸 보는 화면이 따로 없다..
 *  */
suspend fun QuickSightClient.listDataSources(): List<DataSource> {
    val resp = this.listDataSources {
        awsAccountId = awsConfig.awsId
    }
    return resp.dataSources!!
}

/** 없으면 무시 */
suspend fun QuickSightClient.deleteDataSourceIfExist(dataSourceId: String): DeleteDataSourceResponse? {
    return try {
        this.deleteDataSource {
            this.awsAccountId = awsConfig.awsId
            this.dataSourceId = dataSourceId
        }
    } catch (_: ResourceNotFoundException) {
        null
    }
}

/**
 * https://docs.aws.amazon.com/quicksight/latest/user/delete-a-data-source.html
 * S3용 데이터소스 생셩. S3 소스는 수정이 불가능함
 * 적절한 권한이 있어야 , 데이터세트 만들기 할때 데이터 소스가 보인다
 * 데이터소스를 삭제하더라도, SPICE에 있는건 여전히 사용가능
 * */
suspend fun QuickSightClient.createDataSourceS3(id: String, sourceName: String, s3: S3Data, users: List<String>): CreateDataSourceResponse {
    return this.createDataSource {
        awsAccountId = awsConfig.awsId
        dataSourceId = id
        name = sourceName
        type = DataSourceType.S3
        dataSourceParameters = DataSourceParameters.S3Parameters(
            S3Parameters {
                manifestFileLocation {
                    bucket = s3.bucket
                    key = s3.key
                }
            }
        )
        permissions = QuicksightPermissionUtil.toDataSource(awsConfig, users)
    }
}

/**
 * 주의!!
 * 콘솔에서 만들면 권한 문제로 작동을 안하는듯.
 * SDK로 생성하자
 * */
suspend fun QuickSightClient.createDataSourceAthena(id: String, sourceName: String, workGroup: String, users: List<String>): CreateDataSourceResponse {
    return this.createDataSource {
        awsAccountId = awsConfig.awsId
        dataSourceId = id
        name = sourceName
        type = DataSourceType.Athena
        dataSourceParameters = DataSourceParameters.AthenaParameters(
            AthenaParameters {
                this.workGroup = workGroup
            }
        )
        permissions = QuicksightPermissionUtil.toDataSource(awsConfig, users)
    }
}