package net.kotlinx.aws.iam

import aws.sdk.kotlin.services.iam.createAccessKey
import aws.sdk.kotlin.services.iam.deleteAccessKey
import aws.sdk.kotlin.services.iam.listAccessKeys
import aws.sdk.kotlin.services.iam.model.AccessKeyMetadata
import aws.sdk.kotlin.services.iam.model.StatusType
import aws.sdk.kotlin.services.iam.updateAccessKey
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsLocal
import net.kotlinx.aws.toLocalDateTime
import net.kotlinx.number.maxWith
import net.kotlinx.string.toTextGrid
import net.kotlinx.time.toKr01
import net.kotlinx.time.toLong
import net.kotlinx.time.toTimeString
import java.time.Duration

/**
 * 일반적인 보안규정상 영구키값은 주기적으로(약3개월) 교체 해야한다. 이것을 자동으로 해주는 도구
 * 윈도우 전용임!
 * 로컬에서 사용하며, 프로파일 없이 빈 client를 만들어서 호출하면됨
 *  */
class IamSecretUpdateModule() {

    private val log = KotlinLogging.logger {}

    private data class AwsIamKey(val key: AccessKeyMetadata, val limit: Duration) {
        /** 생성시간 */
        val createTime = key.createDate!!.toLocalDateTime()

        /** 얼마나 지났는지 */
        val afterTime: Long = System.currentTimeMillis() - createTime.toLong()

        /** 0이면 정상 */
        val overTime: Long = (afterTime - limit.toMillis()).maxWith(0)
    }

    val client = AwsLocal.CLIENT
    val awsUserName = AwsLocal.AWS_USER_NAME

    /**
     * @param limit Active key age 가 limit 이내가 아니라면 교체
     * */
    fun checkAndUpdate(limit: Duration) = runBlocking {

        val allKeys = client.iam.listAccessKeys { this.userName = awsUserName }.accessKeyMetadata.map { AwsIamKey(it, limit) }
        log.info { "현재 local awsUserName(${awsUserName}) 키값 정보를 출력합니다" }
        listOf("키 ID (아직 교체 전)", "상태", "생성시간", "생성시간 비고", "오버시간").toTextGrid(
            allKeys.map {
                arrayOf(it.key.accessKeyId, it.key.status, it.createTime.toKr01(), "${it.afterTime.toTimeString()} 전 생성됨", it.overTime.toTimeString())
            }
        ).print()

        val invalidKey = allKeys.filter { it.key.status == StatusType.Active && it.overTime > 0L }.maxWithOrNull(compareBy { it.overTime })
        if (invalidKey == null) {
            log.debug { "기간이 지난 키값이 존해자지 않습니다 -> 로직 무시" }
            return@runBlocking
        }

        log.warn { "교체 기간이 지난 키 ${invalidKey.key.accessKeyId} (${invalidKey.afterTime.toTimeString()} 전) 를 새 키로 교체합니다.." }

        allKeys.find { it != invalidKey }?.let {
            log.debug { " -> 가장 오래된 키 ${it.key.accessKeyId} (${it.key.status}) 를 삭제합니다. (키는 2개만 유지 가능)" }
            client.iam.deleteAccessKey {
                this.userName = userName
                this.accessKeyId = it.key.accessKeyId
            }
        }

        val credential = IamCredential()
        val oldKey = credential.keyPair

        check(invalidKey.key.accessKeyId == oldKey.first) { "저장된 키와 invalid 키가 동일해야 합니다." }

        val newKey = client.iam.createAccessKey { this.userName = userName }.let { it.accessKey!!.accessKeyId to it.accessKey!!.secretAccessKey }
        log.info { " -> 키가 교체되어 저장됩니다. ${oldKey.first} => ${newKey.first} / 확인 :  ${credential.secretPath}" }
        credential.replaceKey(newKey)

        log.info { " -> invalid 키 ${oldKey.first} 를 비활성화 시킵니다." }
        client.iam.updateAccessKey {
            this.userName = userName
            this.accessKeyId = oldKey.first
            this.status = StatusType.Inactive
        }

    }


}