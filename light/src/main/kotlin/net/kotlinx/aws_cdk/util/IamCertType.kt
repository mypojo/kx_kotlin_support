package net.kotlinx.aws_cdk.util


/**
 * IAM 인증 타입
 * */
enum class IamCertType {

    /** 직접 해당 계정으로 로그인하는 형태 */
    USER,

    /** ROLE을 만들어서 STS로 권한을 받는 형태 */
    ROLE,

    ;

}