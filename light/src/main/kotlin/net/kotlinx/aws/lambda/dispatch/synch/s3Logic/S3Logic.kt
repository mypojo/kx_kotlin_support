package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

/** 업무 정의 */
class S3Logic {

    /** 유니크한 ID */
    lateinit var id: String

    /** 설명 */
    var desc: List<String> = emptyList()

    /** 실행기 */
    lateinit var runtime: S3LogicRuntime

}