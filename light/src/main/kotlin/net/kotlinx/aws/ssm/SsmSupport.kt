package net.kotlinx.aws.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.getParameter
import aws.sdk.kotlin.services.ssm.model.ParameterNotFound


/**
 * 단축메소드.
 * operator 사용안됨
 * @return 없으면 null 리턴
 *  */
suspend fun SsmClient.find(key: String): String? {
    return try {
        this.getParameter {
            this.name = key
            this.withDecryption = true
        }.parameter?.value
    } catch (e: ParameterNotFound) {
        null
    }
}