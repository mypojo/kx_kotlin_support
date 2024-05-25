package net.kotlinx.awscdk.module

import net.kotlinx.core.Kdsl
import software.amazon.awscdk.SecretValue
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.*

/**
 * 단일 그룹 적용하는 User 간단 생성
 *  */
class CdkIamUserGroup {

    @Kdsl
    constructor(block: CdkIamUserGroup.() -> Unit = {}) {
        apply(block)
    }

    /** 그룹명 */
    lateinit var groupName: String

    /** 사용자 이름들 */
    lateinit var userNames: List<String>

    /**
     * 로그인이 필요한지 여부
     * GIT용 user인 경우 로그인이 필요없음
     * 외부 업체에서 S3 접근등을 해야한다면 로그인이 필요함
     *  */
    var login: Boolean = false

    /** 임시 비밀번호 뒷자리 */
    lateinit var tempPwdSuff: String

    /**
     * 정책들
     * ex) ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess")
     * ex) CdkPolicyStatementSetIam.userDefault()  //기본 로그인
     *  */
    lateinit var managedPolicies: List<IManagedPolicy>

    fun create(stack: Stack) {
        val group = Group(
            stack, "iam_group-$groupName", GroupProps.builder()
                .groupName(groupName)
                .managedPolicies(managedPolicies)
                .build()
        )

        userNames.forEach { userName ->
            User(
                stack, "iam_user-${userName}", UserProps.builder()
                    .groups(listOf(group))
                    .userName(userName)
                    .apply {
                        if (login) {
                            passwordResetRequired(true)
                            password(SecretValue.unsafePlainText("${userName}${tempPwdSuff}"))
                        }
                    }
                    .build()
            )
        }
    }
}