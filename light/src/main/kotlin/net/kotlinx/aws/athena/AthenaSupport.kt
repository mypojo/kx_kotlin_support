package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.athena.AthenaClient
import aws.sdk.kotlin.services.athena.model.ResultConfigurationUpdates
import aws.sdk.kotlin.services.athena.model.ResultSet
import aws.sdk.kotlin.services.athena.updateWorkGroup
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist


val AwsClient.athena: AthenaClient
    get() = getOrCreateClient { AthenaClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * 아테나 결과를 간단 리스트로 변환 (헤더 포함)
 */
val ResultSet.rowLines: List<List<String>>
    get() = this.rows?.map { row ->
        row.data?.map { it.varCharValue ?: "" } ?: emptyList()
    } ?: emptyList()


/**
 * primary 워크그룹의 outputLocation 을 수정해서 쓰레기 버킷이 생기지 않게 해준다
 * ex) 아마존 퀵슈트 등이 사용
 * @param fullPath s3://...
 * */
suspend fun AthenaClient.updateWorkGroupPrimary(fullPath: String?) {
    this.updateWorkGroup {
        workGroup = "primary"
        configurationUpdates {
            resultConfigurationUpdates = ResultConfigurationUpdates {
                outputLocation = fullPath
            }
        }
    }
}