package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.LakeFormationClient
import aws.sdk.kotlin.services.lakeformation.addLfTagsToResource
import aws.sdk.kotlin.services.lakeformation.createLfTag
import aws.sdk.kotlin.services.lakeformation.grantPermissions
import aws.sdk.kotlin.services.lakeformation.model.*
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.regist

val AwsClient.lake: LakeFormationClient
    get() = getOrCreateClient { LakeFormationClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * CDK로도 있는데 그거하면 권한 에러남.. 버그인가?
 * 이게 더 편해서 이걸로 하자.
 * 태그 생성시 자동으로 이 역할에 대해서 태그 key에 대한 모든 권한이 부여된다
 *
 * 아니 인자로 그냥 LfTag를 받지.. 일케 했나?
 * */
suspend fun LakeFormationClient.createLfTag(tag: LfTag): CreateLfTagResponse = this.createLfTag {
    tagKey = tag.tagKey
    tagValues = tag.tagValues
}

/** 데이터베이스에 태그 할당  */
suspend fun LakeFormationClient.addLfTagsToResource(databaseName: String, tags: List<LfTag>, catalogId: String? = null): AddLfTagsToResourceResponse = this.addLfTagsToResource {
    this.resource = Resource {
        this.database = DatabaseResource {
            this.name = databaseName
            this.catalogId = catalogId
        }
    }
    this.lfTags = tags.map {
        LfTagPair {
            this.tagKey = it.tagKey
            this.tagValues = it.tagValues
        }
    }
}

/** 역할에 권한 할당 */
suspend fun LakeFormationClient.grantPermissions(
    roleName: String,
    tags: List<LfTag>,
    target: ResourceType,
    permissions: List<Permission> = listOf(Permission.All)
): GrantPermissionsResponse {
    val awsId = this.awsConfig.awsId
    return this.grantPermissions {
        this.principal = DataLakePrincipal {
            this.dataLakePrincipalIdentifier = "arn:aws:iam::${awsId}:role/${roleName}"
        }
        this.resource = Resource {
            lfTagPolicy = LfTagPolicyResource {
                resourceType = target
                expression = tags
            }
        }
        this.permissions = permissions
    }
}