package net.kotlinx.awscdk.iam

import net.kotlinx.core.Kdsl

class CdkPolicyStatementS3Data {

    @Kdsl
    constructor(block: CdkPolicyStatementS3Data.() -> Unit = {}) {
        apply(block)
    }

    /** 버킷명 */
    lateinit var bucketName: String

    /** 읽기전용 경로 */
    var readonlyPath: List<String> = emptyList()

    /** 쓰기 경로 */
    var writePath: List<String> = emptyList()

}