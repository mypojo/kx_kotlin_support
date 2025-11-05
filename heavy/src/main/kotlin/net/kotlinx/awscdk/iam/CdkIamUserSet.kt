package net.kotlinx.awscdk.iam

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.User
import software.amazon.awscdk.services.iam.UserProps

/**
 * 자주 사용되는 user 모음
 *  */
object CdkIamUserSet {

    /**
     * S3 데이터의 장기 다운로드 링크를 발급하려고 하는경우
     * Presigned URL은 서명 시점에 사용된 크레덴셜의 만료 시각보다 오래 유효할 수 없음
     * 이때문에 영구한 User로 key를 만든디음 이를 파라메터스토어에 넣은후, 프리사인에 사용함
     * */
    fun createUserForPresigned(stack: Stack, userName: String = "app-presigned"): User {
        return User(
            stack, "iam_user-${userName}", UserProps.builder()
                .userName(userName)
                .managedPolicies(
                    listOf(
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess")
                    )
                )
                .build()
        )
    }

}