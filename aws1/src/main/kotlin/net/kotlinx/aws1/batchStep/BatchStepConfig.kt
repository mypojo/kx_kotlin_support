package net.kotlinx.aws1.batchStep

/**
 * 설정된 로직을 실행해주는 도구
 * */
class BatchStepConfig(block: BatchStepConfig.() -> Unit) {

    lateinit var awsId: String

    /** sfn 이름 */
    lateinit var stateMachineName: String

    /** 업로드 버킷 명 */
    lateinit var workUploadBuket: String

    /** 업로드 인풋 경로 */
    var workUploadInputDir: String = "upload/sfnBatchModuleInput/"

    /** 업로드 아웃풋 경로 */
    var workUploadOutputDir: String = "upload/sfnBatchModuleOutput/"

    init {
        block(this)
    }

}


