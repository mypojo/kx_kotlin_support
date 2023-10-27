package net.kotlinx.aws.module.batchStep

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.s3.deleteDir
import net.kotlinx.aws.sfn.SfnUtil

/**
 * S3 베이스의 설정파일
 * */
class BatchStepConfig(block: BatchStepConfig.() -> Unit) {

    lateinit var aws: AwsClient1

    /** 업로드 버킷 명 */
    lateinit var workUploadBuket: String

    /** sfn 이름 */
    lateinit var stateMachineName: String

    /** 람다 이름. List 방식에서 직접 람다를 호출할때 사용함  */
    lateinit var lambdaFunctionName: String

    /** 최종 리포트에 사용 */
    lateinit var athenaModule: AthenaModule

    /** 업로드 인풋 경로 */
    var workUploadInputDir: String = "upload/sfnBatchModuleInput/"

    /** 업로드 아웃풋 경로 */
    var workUploadOutputDir: String = "upload/sfnBatchModuleOutput/"

    init {
        block(this)
    }

    fun consoleLink(sfnId: String): String = SfnUtil.consoleLink(aws.awsConfig.awsId!!, stateMachineName, sfnId)

    /**
     * 업로드 디렉토리를 전부 삭제한다.
     * 주의!! 테스트용으로만 사용할것
     * */
    suspend fun clear() {

        for (i in 0..100) {
            val d1 = aws.s3.deleteDir(workUploadBuket, workUploadInputDir)
            log.warn { "sfnBatchModuleInput 파일 ${d1}건 삭제" }
            if (d1 <= 0) break
        }
        for (i in 0..100) {
            val d2 = aws.s3.deleteDir(workUploadBuket, workUploadOutputDir)
            log.warn { "sfnBatchModuleOutput 파일 ${d2}건 삭제" }
            if (d2 <= 0) break
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}


