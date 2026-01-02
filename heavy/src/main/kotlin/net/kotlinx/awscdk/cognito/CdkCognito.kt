package net.kotlinx.awscdk.cognito

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.awscdk.toCdk
import net.kotlinx.core.Kdsl
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.cognito.*
import kotlin.time.Duration.Companion.days

/**
 * Cognito(User Pool & Client) 구성 헬퍼
 * 이후 Add identity provider 는 콘솔에서 하세요 (보안때문.콘솔 수동 설정이 실무 표준)
 */
open class CdkCognito : CdkInterface {

    @Kdsl
    constructor(block: CdkCognito.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "$baseName-${suff}"

    //==================================================== 풀 설정 ======================================================

    /**
     * 풀 기본 이름 prefix (기본: 프로젝트명)
     * 코그니토는 이름 중복이 가능함
     *  */
    lateinit var baseName: String

    /** 인터넷에 있는 아무 사용자나 User Pool에 계정을 만들 수 있게 할 것인가? (기본: false)  */
    var selfSignUpEnabled: Boolean = false

    /** 로그인 식별자 설정 */
    var signInAliases: SignInAliases = SignInAliases.builder()
        .username(true) //사용자 이름은 불변값이여야 해서 UUID로 내가 입력해줌. loginId나 코그니토 내부 ID(sub) 와는 다름!
        .email(true)  // 이메일로 로그인
        .phone(false) // 전화번호로 로그인 비활성화 -> 활성화시 2차인증에서 SMS 설정 필요함
        .preferredUsername(false)  //익명 같은거로 로그인
        .build()

    /** 대소문자 구분 (기본: true) */
    var signInCaseSensitive: Boolean = true

    /** 기본 비밀번호 정책 (요청 예제 값 적용) */
    var passwordPolicy: PasswordPolicy = PasswordPolicy.builder()
        .minLength(8)
        .requireLowercase(true)
        .requireUppercase(true)
        .requireDigits(true)
        .requireSymbols(true)
        .tempPasswordValidity(7.days.toCdk())
        .build()

    /** MFA 설정 (기본: OPTIONAL, OTP 사용).  보통 관리자만 사용  */
    var mfa: Mfa = Mfa.OPTIONAL

    var mfaSecondFactor: MfaSecondFactor = MfaSecondFactor.builder()
        .otp(true)
        .sms(false)
        .build()

    /** 계정 복구 방식 (기본: EMAIL_ONLY) */
    var accountRecovery: AccountRecovery = AccountRecovery.EMAIL_ONLY

    /** 이메일 발송 설정 (기본: Cognito 기본 이메일) */
    var email: UserPoolEmail = UserPoolEmail.withCognito()

    /** 삭제 보호 (PROD 기본 true) */
    var deletionProtection: Boolean = deploymentType == DeploymentType.PROD

    //==================================================== 클라이언트 설정 제거: CdkCognitoClient 로 분리 ======================================================

    /**
     * 커스텀 도메인 설정
     * ex) auth.xxx.com
     *
     * 주의!! 부모 도메인에 임시로라도 A 레코드가 있어야함
     * 아직 라이브 서버구성이 안된 상태인경우
     * 콘소에서 임시로 루트 도메인에 a 레코드로 1.1.1.1 을 입력하면됨 -> 향후 www.domain.com 으로 변경
     *  */
    var customDomain: Pair<String, ICertificate>? = null

    //==================================================== 결과 ======================================================

    lateinit var userPool: UserPool
    var domain: UserPoolDomain? = null

    fun create(stack: Stack, userPoolBlock: UserPool.Builder.() -> Unit = {}): CdkCognito {
        // User Pool
        userPool = UserPool.Builder.create(stack, "userPool-${logicalName}")
            .userPoolName(logicalName)
            .selfSignUpEnabled(selfSignUpEnabled)
            .signInAliases(signInAliases)
            .signInCaseSensitive(signInCaseSensitive)
            .passwordPolicy(passwordPolicy)
            .mfa(mfa)
            .mfaSecondFactor(mfaSecondFactor)
            .accountRecovery(accountRecovery)
            .email(email)
            .deletionProtection(deletionProtection)
            .apply(userPoolBlock)
            .build()

        // 기본으로 커스텀 도메인 사용
        customDomain?.let {
            domain = userPool.addDomain(
                "userPoolDomainCustom-${logicalName}",
                UserPoolDomainOptions.builder()
                    .customDomain(
                        CustomDomainOptions.builder()
                            .domainName(it.first)
                            .certificate(it.second)
                            .build()
                    )
                    .build()
            )
            // User Pool이 먼저 생성된 후 도메인이 생성되도록 의존성 추가
            domain!!.node.addDependency(userPool)
        }

        TagUtil.tagDefault(userPool)
        domain?.let { TagUtil.tagDefault(it) }
        return this
    }

}
