package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.*
import aws.sdk.kotlin.services.quicksight.model.*
import aws.sdk.kotlin.services.quicksight.paginators.listFoldersForResourcePaginated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.toKoreaDateTime
import net.kotlinx.collection.doUntilTokenNull
import net.kotlinx.concurrent.coroutine
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01

/** 간단 출력 */
fun List<Folder>.printSimple() {
    listOf("id", "name", "folderPath", "type", "sharingModel", "createdTime", "lastUpdatedTime").toTextGridPrint {
        this.map {
            arrayOf(
                it.folderId,
                it.name,
                it.folderPath?.joinToString("->") ?: "-",
                it.folderType,
                it.sharingModel,
                it.createdTime!!.toKoreaDateTime().toKr01(),
                it.lastUpdatedTime!!.toKoreaDateTime().toKr01()
            )
        }
    }
}

/**
 * 폴더에 들어가는것 -> 데이터세트 & 분석
 * */
suspend fun QuickSightClient.createFolder(id: String, name: String, parentId: String?, users: List<String>): CreateFolderResponse {
    return this.createFolder {
        this.awsAccountId = awsConfig.awsId
        this.folderId = id
        this.name = name
        this.folderType = FolderType.Shared //그냥 고정
        this.permissions = QuicksightPermissionUtil.toFolder(awsConfig, users)
        parentId?.let { pid ->
            this.parentFolderArn = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:folder/${pid}"
        }
    }
}

/** 이름 정도만 변경 가능한듯 */
suspend fun QuickSightClient.updateFolder(id: String, name: String): UpdateFolderResponse {
    return this.updateFolder {
        this.awsAccountId = awsConfig.awsId
        this.folderId = id
        this.name = name
    }
}

/** 폴더 리스팅 */
@Deprecated("사용안함")
suspend fun QuickSightClient.listFolders2(): List<Folder> {
    val folderSummarys = doUntilTokenNull { i, token ->
        val response = this.listFolders {
            this.awsAccountId = awsConfig.awsId
            this.nextToken = token as String?
        }
        response.folderSummaryList!! to response.nextToken
    }.flatten<FolderSummary>()

    return folderSummarys.map { summary ->
        suspend {
            this.describeFolder {
                awsAccountId = awsConfig.awsId
                folderId = summary.folderId
            }.folder!!
        }
    }.coroutineExecute(20)
}

/** 작동 확인 필요 */
fun QuickSightClient.listFolders(): Flow<Folder> {
    return this.listFoldersForResourcePaginated { this.awsAccountId = awsConfig.awsId }.flatMapConcat {
        val folderIds = it.folders!!
        folderIds.coroutine { id ->
            describeFolder {
                awsAccountId = awsConfig.awsId
                folderId = id
            }.folder!!
        }.asFlow()
    }
}

/** 폴더 삭제 (UI에서 안보이는경우) */
suspend fun QuickSightClient.deleteFolder(id: String): DeleteFolderResponse {
    return this.deleteFolder {
        this.awsAccountId = awsConfig.awsId
        this.folderId = id
    }
}