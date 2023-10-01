package net.kotlinx.aws.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.getParameter


/**
 * 단축메소드.
 * operator 사용안됨
 *  */
suspend fun SsmClient.find(key: String): String? {
    return this.getParameter {
        this.name = key
        this.withDecryption = true
    }.parameter?.value
}
