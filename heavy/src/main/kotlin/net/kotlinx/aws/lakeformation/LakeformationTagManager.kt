package net.kotlinx.aws.lakeformation

import aws.sdk.kotlin.services.lakeformation.model.InvalidInputException
import aws.sdk.kotlin.services.lakeformation.model.LfTag
import aws.sdk.kotlin.services.lakeformation.model.Permission
import aws.sdk.kotlin.services.lakeformation.model.ResourceType
import net.kotlinx.aws.AwsClient
import net.kotlinx.core.Kdsl


/**
 * 레이크 포메이션의 경우 IAM 과는 별도로 내부 권한을 설정해야 한다
 * LF 태그 위주로 작업 할것!!
 *
 * 다수를 만든 후 조합해서 사용할것
 * */
class LakeformationTagManager {

    @Kdsl
    constructor(block: LakeformationTagManager.() -> Unit = {}) {
        apply(block)
    }

    /** AWS 클라이언트 */
    lateinit var aws: AwsClient

    /** LF 태그들 */
    lateinit var tags: List<LfTag>

    /**
     * LF 태그 사용 권한을 줄 역할
     * ex) app-admin
     *  */
    lateinit var roleName: String

    /**
     * LF 태그를 할당할 데이터베이스들
     * */
    lateinit var databaseNames: List<String>

    //==================================================== 실행 ======================================================

    /** 태그 생성. 이미 있으면 무시 */
    suspend fun createLfTag() {
        tags.forEach {
            try {
                aws.lake.createLfTag(it)
            } catch (e: InvalidInputException) {
                //무시함
            }
        }
    }

    /**
     * 역할 & 리소스(데이터베이스 & 테이블)에 태그 할당
     *  */
    suspend fun grantPermissions() {
        aws.lake.grantPermissions(roleName, tags, ResourceType.Database)
        aws.lake.grantPermissions(roleName, tags, ResourceType.Table)
    }

    /**
     * 읽기 전용으로 권한 할당
     * */
    suspend fun grantPermissionsReadonly() {
        aws.lake.grantPermissions(roleName, tags, ResourceType.Database, listOf(Permission.Describe))
        aws.lake.grantPermissions(roleName, tags, ResourceType.Table, listOf(Permission.Select, Permission.Describe))
    }

    /**
     * 각 데이터베이스에 태그 할당 (테이블은 상속됨)
     *  */
    suspend fun addLfTagsToResource() {
        databaseNames.forEach {
            aws.lake.addLfTagsToResource(it, tags)
        }
    }


}