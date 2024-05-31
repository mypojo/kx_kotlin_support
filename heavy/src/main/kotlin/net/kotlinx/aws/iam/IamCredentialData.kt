package net.kotlinx.aws.iam

import net.kotlinx.regex.extract
import net.kotlinx.string.mask

/** 취급 주의!! */
data class IamCredentialData(
    val profileName: String,
    val accessKey: String? = null,
    val secretKey: String? = null,
    val roleArn: String? = null
) {

    val awsId: String? = roleArn?.extract("arn:aws:iam::" to ":role/")

    val desc: String by lazy {
        when {
            accessKey != null -> "키페어 : ${accessKey.mask()}"
            roleArn != null -> "STS : $awsId"
            else -> throw IllegalStateException()
        }
    }
}