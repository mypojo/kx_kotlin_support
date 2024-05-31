package net.kotlinx.aws.iam

import net.kotlinx.collection.groupByFirstCondition
import net.kotlinx.regex.extract
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * 가능하면 메모리에 남기지 않게 하기위해서 static하게 만들지 않음
 * */
class IamCredential {

    /**
     * 일반적인 로컬 시크릿 위치.
     *  */
    val secretPath: Path by lazy {
        val profile = System.getenv(ENV_PROFILE)
        listOf(
            Paths.get(profile, AWS_CREDENTIAL_PATH01),
            Paths.get(profile, AWS_CREDENTIAL_PATH02),
        ).firstOrNull { it.toFile().exists() } ?: throw IllegalStateException("AWS 설정파일이 없습니다. from $profile")
    }

    val secretFileText by lazy { secretPath.readText() }

    /**
     * 프로파일 정보들
     * ex) 전체 계정의 과금 로드
     * ex) 특정 프로파일 메타데이터 참조
     *  */
    val profileDatas: List<IamCredentialData> by lazy {
        val profileTexts = secretFileText.split("\n").groupByFirstCondition { it.startsWith("[") }
        profileTexts.map { lines ->
            val profileName = lines.first().extract("[" to "]")!!
            IamCredentialData(
                profileName,
                lines.find { it.startsWith(AWS_ACCESS_KEY_ID) }?.substringAfter("=")?.trim(),
                lines.find { it.startsWith(AWS_SECRET_ACCESS_KEY) }?.substringAfter("=")?.trim(),
                lines.find { it.startsWith(ROLE_ARN) }?.substringAfter("=")?.trim(),
            )
        }
    }

    /**
     * 키값이 하나이상 있어야 정상작동한다.
     * 가장 위의 키를 사용
     *  */
    val keyPair: Pair<String, String> by lazy { profileDatas.first().let { it.accessKey!! to it.secretKey!! } }


    /** 키 값을 교체한다. */
    fun replaceKey(newKey: Pair<String, String>) {
        val secretFileText = secretPath.readText()
        val newSecretFileText = secretFileText.replace(keyPair.first, newKey.first).replace(keyPair.second, newKey.second)
        secretPath.writeText(newSecretFileText)
    }

    companion object {
        /**
         * 기본 설정 위치
         * https://docs.aws.amazon.com/ko_kr/sdkref/latest/guide/file-location.html
         * */
        private const val AWS_CREDENTIAL_PATH01 = ".aws/credentials"
        private const val AWS_CREDENTIAL_PATH02 = ".aws/config"

        private const val ENV_PROFILE = "USERPROFILE"

        private const val AWS_ACCESS_KEY_ID = "aws_access_key_id"
        private const val AWS_SECRET_ACCESS_KEY = "aws_secret_access_key"
        private const val ROLE_ARN = "role_arn"

    }


}
