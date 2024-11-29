package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.createFolder
import aws.sdk.kotlin.services.quicksight.model.CreateFolderResponse
import net.kotlinx.aws.awsConfig


/**
 * 폴더에 들어가는것 -> 데이터세트 & 분석
 * */
suspend fun QuickSightClient.createFolder(id: String, name: String): CreateFolderResponse {
    return this.createFolder {
        this.awsAccountId = awsConfig.awsId
        this.folderId = id
        this.name = name
    }
}