package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.*
import aws.sdk.kotlin.services.cognitoidentityprovider.model.*
import aws.sdk.kotlin.services.cognitoidentityprovider.paginators.listUsersPaginated
import kotlinx.coroutines.flow.Flow
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty


/**
 * 코그니토 백엔드용 컴포넌트
 * */
class CognitoComponent(private val cognitoId: String, private val cognitoClientId: String) {

    /** AwsClient 지연 주입 */
    var aws: AwsClient by LazyAwsClientProperty()

    //==================================================== 조회 ======================================================

    /** 모든 사용자 목록을 가져옵니다. */
    fun listAllUsers(): Flow<ListUsersResponse> = aws.cognito.listUsersPaginated { this.userPoolId = cognitoId }

    /** 간단한 유저 정보 조회 */
    suspend fun adminGetUser(username: String): AdminGetUserResponse {
        return aws.cognito.adminGetUser {
            this.userPoolId = cognitoId
            this.username = username
        }
    }

    /**
     * 백오피스용 사용자 속성 업데이트
     * - 이메일 또는 전화번호 중 제공된 것만 업데이트
     * - email 변경 시 email_verified 를 true 로 재설정
     * - phone_number 변경 시 phone_number_verified 를 true 로 재설정
     * - 두 파라미터가 모두 null 이면 아무 작업도 수행하지 않음
     */
    suspend fun adminUpdateUserDefault(username: String, email: String?, phoneNumber: String?) {
        val attrs = buildList {
            if (email != null) {
                add(
                    AttributeType {
                        name = "email"
                        value = email
                    }
                )
                add(
                    AttributeType {
                        name = "email_verified"
                        value = "true"
                    }
                )
            }
            if (phoneNumber != null) {
                add(
                    AttributeType {
                        name = "phone_number"
                        value = phoneNumber
                    }
                )
                add(
                    AttributeType {
                        name = "phone_number_verified"
                        value = "true"
                    }
                )
            }
        }

        if (attrs.isEmpty()) return

        aws.cognito.adminUpdateUserAttributes {
            userPoolId = cognitoId
            this.username = username
            userAttributes = attrs
        }
    }


    //==================================================== 생성/수정 ======================================================

    /**
     * 백오피스용 사용자 생성
     * 주의! 이 사용자는 member_login 과 1:1 로 매핑된다
     * @param username UUID로 사용해도됨
     *
     * 임시 비번발급 & 변경 로직은 시간관계상 백오피스에서는 생략
     */
    suspend fun adminCreateUserDefault(
        username: String,
        email: String,
        permanentPassword: String,
        phoneNumber: String? = null,
        groupName: String? = null,
    ) {
        // 1️⃣ 사용자 생성 (메일 발송 없음)
        try {
            aws.cognito.adminCreateUser {
                userPoolId = cognitoId
                this.username = username
                userAttributes = buildList {
                    add(
                        AttributeType {
                            name = "email"
                            value = email
                        }
                    )
                    add(
                        AttributeType {
                            name = "email_verified"
                            value = "true"
                        }
                    )
                    if (phoneNumber != null) {
                        add(
                            AttributeType {
                                name = "phone_number"
                                value = phoneNumber
                            }
                        )
                        add(
                            AttributeType {
                                name = "phone_number_verified"
                                value = "true"
                            }
                        )
                    }
                }
                messageAction = MessageActionType.Suppress  //Cognito가 이메일/임시 비밀번호 발송하지 않음.
            }
        } catch (_: UsernameExistsException) {
            //무시한다. 트랜잭션이 없어서 뒤에꺼만 호출할수도 있음
        }

        // 2️⃣ 영구 비밀번호 설정
        adminSetUserPasswordDefault(username, permanentPassword)

        // 3️⃣ 그룹 추가 (요청시)
        if (groupName != null) {
            aws.cognito.adminAddUserToGroup {
                userPoolId = cognitoId
                this.username = username
                this.groupName = groupName
            }
        }

    }

    /**
     * 관리자가 특정 사용자의 비밀번호를 강제로 변경
     * 오픈 서비스에서는 이렇게 하면 위험!
     *  */
    suspend fun adminSetUserPasswordDefault(username: String, newPassword: String) {
        aws.cognito.adminSetUserPassword {
            userPoolId = cognitoId
            this.username = username
            password = newPassword
            permanent = true
        }
    }

    //==================================================== 삭제 ======================================================

    /**
     * 관리자가 사용자를 영구 삭제합니다.
     * 복구 불가. 주의해서 사용할 것.
     */
    suspend fun adminDeleteUser(username: String) {
        aws.cognito.adminDeleteUser {
            userPoolId = cognitoId
            this.username = username
        }
    }


}

