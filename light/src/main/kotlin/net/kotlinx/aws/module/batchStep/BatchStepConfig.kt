package net.kotlinx.aws.module.batchStep

import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.deleteDir
import net.kotlinx.aws.s3.toList
import net.kotlinx.aws.sfn.SfnUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BatchStep 설정파일
 * */
class BatchStepConfig(block: BatchStepConfig.() -> Unit) : KoinComponent {

    private val aws1: AwsClient1 by inject()

    /** 업로드 버킷 명 */
    lateinit var workUploadBuket: String

    /** sfn 이름 */
    lateinit var stateMachineName: String

    /** 람다 이름. List 방식에서 직접 람다를 호출할때 사용함  */
    lateinit var lambdaFunctionName: String

    /** 업로드 인풋 경로 */
    var workUploadInputDir: String = "upload/sfnBatchModuleInput/"

    /** 업로드 아웃풋 경로 */
    var workUploadOutputDir: String = "upload/sfnBatchModuleOutput/"

    init {
        block(this)
    }

    /** 콘솔링크 출력 */
    fun consoleLink(sfnId: String): String = SfnUtil.consoleLink(aws1.awsConfig.awsId!!, stateMachineName, sfnId)

    //==================================================== 편의셩 유틸 ======================================================

    /** 인풋 데이터들을 리스팅한다 */
    suspend fun listInputs(targetSfnId: String): List<String> {
        return aws1.s3.listObjectsV2Paginated {
            this.bucket = workUploadBuket
            this.prefix = "${workUploadInputDir}${targetSfnId}/"
        }.toList()
    }

    /**
     * 업로드 디렉토리를 전부 삭제한다.
     * 주의!! 테스트용으로만 사용할것
     * */
    suspend fun clear() {

        for (i in 0..100) {
            val d1 = aws1.s3.deleteDir(workUploadBuket, workUploadInputDir)
            log.warn { "sfnBatchModuleInput 파일 ${d1}건 삭제" }
            if (d1 <= 0) break
        }
        for (i in 0..100) {
            val d2 = aws1.s3.deleteDir(workUploadBuket, workUploadOutputDir)
            log.warn { "sfnBatchModuleOutput 파일 ${d2}건 삭제" }
            if (d2 <= 0) break
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}


