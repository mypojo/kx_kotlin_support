package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.athena.AthenaClient
import aws.sdk.kotlin.services.athena.model.ResultSet
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