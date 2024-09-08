package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.LakeFormationClient
import aws.sdk.kotlin.services.lakeformation.addLfTagsToResource
import aws.sdk.kotlin.services.lakeformation.createLfTag
import aws.sdk.kotlin.services.lakeformation.model.*

typealias LfTag = Pair<String, List<String>>

/**
 * CDK로도 있는데 그거하면 권한 에러남.. 버그인가?
 * 이게 더 편해서 이걸로 하자.
 * 태그 생성시 자동으로 이 역할에 대해서 태그 key에 대한 모든 권한이 부여된다
 * */
suspend fun LakeFormationClient.createLfTag(tag: LfTag): CreateLfTagResponse = this.createLfTag {
    tagKey = tag.first
    tagValues = tag.second
}

/** 데이터베이스에 태그 할당  */
suspend fun LakeFormationClient.addLfTagsToResource(databaseName: String, tags: List<LfTag>): AddLfTagsToResourceResponse = this.addLfTagsToResource {
    this.resource = Resource {
        this.database = DatabaseResource {
            this.name = databaseName
        }
    }
    this.lfTags = tags.map {
        LfTagPair {
            this.tagKey = it.first
            this.tagValues = it.second
        }
    }
}