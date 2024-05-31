package net.kotlinx.aws.iam

import aws.sdk.kotlin.services.iam.*
import aws.sdk.kotlin.services.iam.model.AccessKeyMetadata
import aws.sdk.kotlin.services.iam.model.StatusType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
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
 *  */
class IamSecretUpdateModule(
    private val iamClient: IamClient,
) {

    private val log = KotlinLogging.logger {}

    private data class AwsIamKey(val key: AccessKeyMetadata, val limit: Duration) {
        /** 생성시간 */
        val createTime = key.createDate!!.toLocalDateTime()

        /** 얼마나 지났는지 */
        val afterTime: Long = System.currentTimeMillis() - createTime.toLong()

        /** 0이면 정상 */
        val overTime: Long = (afterTime - limit.toMillis()).maxWith(0)
    }

    /**
     * @param userName 환경변수 등으로 설정할것.
     * @param limit Active key age 가 limit 이내가 아니라면 교체
     * */
    fun checkAndUpdate(userName: String, limit: Duration) = runBlocking {

        val allKeys = iamClient.listAccessKeys { this.userName = userName }.accessKeyMetadata.map { AwsIamKey(it, limit) }
        log.info { "현재 키값 정보를 출력합니다" }
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
            iamClient.deleteAccessKey {
                this.userName = userName
                this.accessKeyId = it.key.accessKeyId
            }
        }

        val credential = IamCredential()
        val oldKey = credential.keyPair

        check(invalidKey.key.accessKeyId == oldKey.first) { "저장된 키와 invalid 키가 동일해야 합니다." }

        val newKey = iamClient.createAccessKey { this.userName = userName }.let { it.accessKey!!.accessKeyId to it.accessKey!!.secretAccessKey }
        log.info { " -> 키가 교체되어 저장됩니다. ${oldKey.first} => ${newKey.first} / 확인 :  ${credential.secretPath}" }
        credential.replaceKey(newKey)

        log.info { " -> invalid 키 ${oldKey.first} 를 비활성화 시킵니다." }
        iamClient.updateAccessKey {
            this.userName = userName
            this.accessKeyId = oldKey.first
            this.status = StatusType.Inactive
        }

    }


}