package net.kotlinx.aws_cdk


data class CdkProject(
    /** AWS ID ex) 653734769926 */
    val awsId: String,
    /** 이거 기반으로 네이밍 */
    val projectName: String,
    /** 리즌 */
    val region: String = "ap-northeast-2",
)