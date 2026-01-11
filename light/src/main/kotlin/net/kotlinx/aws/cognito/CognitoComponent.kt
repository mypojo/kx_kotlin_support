package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.*
import aws.sdk.kotlin.services.cognitoidentityprovider.model.*
import aws.sdk.kotlin.services.cognitoidentityprovider.paginators.listUsersPaginated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty


/**
 * 코그니토 백엔드용 컴포넌트.
 * */
class CognitoComponent(val cognitoId: String) {

    /** AwsClient 지연 주입 */
    var aws: AwsClient by LazyAwsClientProperty()

    //==================================================== 조회 ======================================================

    /** 모든 사용자 목록을 가져옵니다. */
    fun listAllUsers(): Flow<ListUsersResponse> = aws.cognito.listUsersPaginated { this.userPoolId = cognitoId }

    /**
     * username 접두어로 사용자 목록을 가져옵니다.
     * 소스코드 참고용
     *  */
    fun listUsersByUsernamePrefix(usernamePrefix: String): Flow<ListUsersResponse> = aws.cognito.listUsersPaginated {
        this.userPoolId = cognitoId
        this.filter = "username ^= \"$usernamePrefix\""
    }

    /**
     * 간단한 유저 정보 조회
     * username 버전은 거의 쓰지 않음
     *  */
    suspend fun findUserByUsername(username: String): AdminGetUserResponse {
        return aws.cognito.adminGetUser {
            this.userPoolId = cognitoId
            this.username = username
        }
    }

    /**
     * sub으로 사용자 정보를 가져옵니다.
     * 가장 많이 사용함!
     *  */
    suspend fun findUserBySub(sub: String): UserType? = aws.cognito.listUsersPaginated {
        this.userPoolId = cognitoId
        this.filter = "sub = \"$sub\""
        this.limit = 1
    }.map { it.users?.firstOrNull() }.firstOrNull()

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
    suspend fun adminCreateUserDefault(username: String, email: String, permanentPassword: String, phoneNumber: String? = null, groupName: String? = null): UserType {
        // 1️⃣ 사용자 생성 (메일 발송 없음)
        val user = try {
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
            }.user!!
        } catch (_: UsernameExistsException) {
            log.info { "사용자가 이미 존재하여 기존 사용자를 조회합니다. username: $username" }
            findUserByUsername(username).let {
                //변환해준다..
                UserType {
                    this.username = it.username
                    this.attributes = it.userAttributes
                    this.userCreateDate = it.userCreateDate
                    this.userLastModifiedDate = it.userLastModifiedDate
                    this.enabled = it.enabled
                    this.userStatus = it.userStatus
                }
            }
        }

        // 2️⃣ 영구 비밀번호 설정
        adminSetUserPasswordDefault(username, permanentPassword)

        // 3️⃣ 그룹 추가 (요청시)
        if (groupName != null) {
            try {
                aws.cognito.adminAddUserToGroup {
                    userPoolId = cognitoId
                    this.username = username
                    this.groupName = groupName
                }
            } catch (_: ResourceNotFoundException) {
                log.warn { "User 생성시 그룹이 존재하지 않아 생성합니다: $groupName" }
                aws.cognito.createGroup {
                    userPoolId = cognitoId
                    this.groupName = groupName
                }
                aws.cognito.adminAddUserToGroup {
                    userPoolId = cognitoId
                    this.username = username
                    this.groupName = groupName
                }
            }
        }
        return user
    }

    /**
     * 관리자가 특정 사용자의 비밀번호를 강제로 변경
     * 오픈 서비스에서는 이렇게 하면 위험!
     *  */
    suspend fun adminSetUserPasswordDefault(username: String, newPassword: String): AdminSetUserPasswordResponse {
        return aws.cognito.adminSetUserPassword {
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
    suspend fun adminDeleteUser(username: String): AdminDeleteUserResponse {
        return aws.cognito.adminDeleteUser {
            userPoolId = cognitoId
            this.username = username
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}

