package net.kotlinx.core

/**
 * 프로토콜의 접두사 모음집
 * 전부 소문자!
 * */
object ProtocolPrefix {

    /**
     * https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/dynamic-references.html
     * ex) "${ProtocolPrefix.SSM}/gpt4/demo/key"
     *  */
    const val SSM = "resolve:ssm:"

    /** S3 */
    const val S3 = "s3://"

    /** HTTP */
    const val HTTP = "http://"

    /** HTTPS */
    const val HTTPS = "https://"


}