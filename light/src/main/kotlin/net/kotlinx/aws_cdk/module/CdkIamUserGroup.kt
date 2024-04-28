package net.kotlinx.aws_cdk.module

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
                    .passwordResetRequired(true)
                    .password(SecretValue.unsafePlainText("${userName}${tempPwdSuff}"))
                    .build()
            )
        }
    }
}