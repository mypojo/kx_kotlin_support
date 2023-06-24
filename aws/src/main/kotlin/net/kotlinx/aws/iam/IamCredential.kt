package net.kotlinx.aws.iam

import net.kotlinx.core.regex.RegexSet
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.io.path.writeText

class IamCredential {

    /** 일반적인 로컬 시크릿 위치.  */
    val secretPath: Path by lazy {
        val profile = System.getenv(ENV_PROFILE)
        listOf(
            Paths.get(profile, AWS_CREDENTIAL_PATH01),
            Paths.get(profile, AWS_CREDENTIAL_PATH02),
        ).firstOrNull { it.toFile().exists() } ?: throw IllegalStateException("AWS 설정파일이 없습니다. from $profile")
    }

    val secretFileText by lazy { secretPath.readText() }

    /** 키값이 하나만 존재해야 한다. */
    val keyPair by lazy {
        val accessKey = ACCESS_KEY.find(secretFileText)?.value?.trim() ?: throw IllegalStateException("억세스 키가 없습니다.")
        val secretKey = SECRET_KEY.find(secretFileText)?.value?.trim() ?: throw IllegalStateException("시크릿 키가 없습니다.")
        accessKey to secretKey
    }

    /** 프로파일 이름들. 전체 프로파일을 로드할때 사용 ex) 과금 로드 */
    val profileNames by lazy {
        val regex = RegexSet.extract("[", "]").toRegex()
        regex.findAll(IamCredential().secretFileText).map { it.value }.filter { it != "default" }.toList()
    }

    /** 키 값을 교체한다. */
    fun replaceKey(newKey: Pair<String, String>) {
        val secretFileText = secretPath.readText()
        val newSecretFileText = secretFileText.replace(keyPair.first, newKey.first).replace(keyPair.second, newKey.second)
        secretPath.writeText(newSecretFileText)
    }

    fun asd(newKey: Pair<String, String>) {

    }

    companion object {
        /**
         * 기본 설정 위치
         * https://docs.aws.amazon.com/ko_kr/sdkref/latest/guide/file-location.html
         * */
        private const val AWS_CREDENTIAL_PATH01 = ".aws/credentials"
        private const val AWS_CREDENTIAL_PATH02 = ".aws/config"

        private const val ENV_PROFILE = "USERPROFILE"

        private val ACCESS_KEY = "(?<=aws_access_key_id\\s{0,2}=).*".toRegex(RegexOption.MULTILINE)
        private val SECRET_KEY = "(?<=aws_secret_access_key\\s{0,2}=).*".toRegex(RegexOption.MULTILINE)

    }


}
