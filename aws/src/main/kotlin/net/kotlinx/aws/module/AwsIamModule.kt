package net.kotlinx.aws.module

import aws.sdk.kotlin.services.iam.*
import aws.sdk.kotlin.services.iam.model.AccessKeyMetadata
import aws.sdk.kotlin.services.iam.model.StatusType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws1.toLocalDateTime
import net.kotlinx.core1.number.maxWith
import net.kotlinx.core1.number.toTimeString
import net.kotlinx.core1.string.toTextGrid
import net.kotlinx.core1.time.toKr01
import net.kotlinx.core1.time.toLong
import java.nio.file.Paths
import java.time.Duration
import kotlin.io.path.readText
import kotlin.io.path.writeText

/** iam 관련 처리기 */
class AwsIamModule(
    private val iamClient: IamClient,
) {

    private val log = KotlinLogging.logger {}

    private data class AwsIamKey(
        val key: AccessKeyMetadata,
        val duration: Duration,
    ) {
        /** 생성시간 */
        val createTime = key.createDate!!.toLocalDateTime()

        /** 얼마나 지났는지 */
        val afterTime: Long = System.currentTimeMillis() - createTime.toLong()

        /** 0이면 정상 */
        val overTime: Long = (afterTime - duration.toMillis()).maxWith(0)
    }

    /**
     * 일반적인 보안규정상 영구키값은 주기적으로(약3개월) 교체 해야한다. 이것을 자동으로 해주는 도구
     * Active key age 가 duration 이내여야 한다.
     * @param userName 환경변수 등으로 설정할것.
     * */
    fun changeLocalSecretKey(userName: String, duration: Duration) = runBlocking {

        val allKeys = iamClient.listAccessKeys { this.userName = userName }.accessKeyMetadata!!.map { AwsIamKey(it, duration) }
        if (log.isDebugEnabled) {
            listOf("키(교체 전)", "상태", "생성시간", "생성시간 비고", "오버시간").toTextGrid(
                allKeys.map {
                    arrayOf(it.key.accessKeyId, it.key.status, it.createTime.toKr01(), "${it.afterTime.toTimeString()} 전 생성됨", it.overTime.toTimeString())
                }
            ).print()
        }

        val invalid = allKeys.filter { it.key.status == StatusType.Active && it.overTime > 0L }.maxWithOrNull(compareBy { it.overTime }).apply {
            if (this == null) {
                log.debug { "기간이 지난 키값이 존해자지 않습니다" }
                return@runBlocking
            }
            log.info { "교체 기간이 지난 키 ${this.key.accessKeyId} (${this.afterTime.toTimeString()} 전) 를 새 키로 교체합니다.." }
        }!!

        allKeys.find { it != invalid }?.let {
            log.debug { " -> 가장 오래된 키 ${it.key.accessKeyId} (${it.key.status}) 를 삭제합니다. (키는 2개만 유지 가능)" }
            iamClient.deleteAccessKey {
                this.userName = userName
                this.accessKeyId = it.key.accessKeyId
            }
        }

        val secretFilePath = System.getenv("USERPROFILE").let { userProfile ->
            Paths.get(userProfile, AWS_CREDENTIAL_PATH).also {
                check(it.toFile().exists()) { "AWS 설정파일이 없습니다. $it" }
            }
        }
        val secretFileText = secretFilePath.readText()

        val oldKey = run {
            val accessKey = ACCESS_KEY.find(secretFileText)?.value?.trim() ?: throw IllegalStateException("억세스 키가 없습니다.")
            val secretKey = SECRET_KEY.find(secretFileText)?.value?.trim() ?: throw IllegalStateException("시크릿 키가 없습니다.")
            accessKey to secretKey
        }
        check(invalid.key.accessKeyId == oldKey.first) { "저장된 키와 invalid 키가 동일해야 합니다." }

        val newKey = iamClient.createAccessKey { this.userName = userName }.let { it.accessKey!!.accessKeyId!! to it.accessKey!!.secretAccessKey!! }
        log.info { " -> 키가 교체되어 저장됩니다. ${oldKey.first} => ${newKey.first} / 확인 :  $secretFilePath" }

        val newSecretFileText = secretFileText.replace(oldKey.first, newKey.first).replace(oldKey.second, newKey.second)
        secretFilePath.writeText(newSecretFileText)

        log.debug { " -> invalid 키 ${oldKey.first} 를 비활성화 시킵니다." }
        iamClient.updateAccessKey {
            this.userName = userName
            this.accessKeyId = oldKey.first
            this.status = StatusType.Inactive
        }

    }

    companion object {
        /** 기본 설정 위치 */
        private const val AWS_CREDENTIAL_PATH = ".aws/credentials"
        private val ACCESS_KEY = "(?<=aws_access_key_id\\s{0,2}=).*".toRegex(RegexOption.MULTILINE)
        private val SECRET_KEY = "(?<=aws_secret_access_key\\s{0,2}=).*".toRegex(RegexOption.MULTILINE)
    }

}