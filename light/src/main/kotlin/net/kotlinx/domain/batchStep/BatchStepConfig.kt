package net.kotlinx.domain.batchStep

import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import aws.sdk.kotlin.services.sfn.describeExecution
import aws.sdk.kotlin.services.sfn.model.DescribeExecutionResponse
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.s3.deleteDir
import net.kotlinx.aws.s3.s3
import net.kotlinx.aws.sfn.sfn
import net.kotlinx.core.Kdsl
import net.kotlinx.domain.batchStep.stepDefault.StepEnd
import net.kotlinx.domain.batchStep.stepDefault.StepList
import net.kotlinx.domain.batchStep.stepDefault.StepStart
import net.kotlinx.koin.Koins.koinLazy

/**
 * BatchStep 설정파일
 * */
class BatchStepConfig {

    @Kdsl
    constructor(block: BatchStepConfig.() -> Unit = {}) {
        apply(block)
    }

    private val aws: AwsClient by koinLazy()

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

    /** 콘솔링크 출력 */
    fun consoleLink(sfnId: String): String = aws.awsConfig.sfnConfig.consoleLink(stateMachineName, sfnId)

    /** 미리 준비된 로직들 */
    var logics: List<BatchStepLogic> = listOf(
        StepStart(),
        StepList(),
        StepEnd(),
    )

    //==================================================== 편의셩 유틸 ======================================================

    /** 인풋 데이터들을 리스팅한다 */
    suspend fun listInputs(targetSfnId: String): List<String> {
        return aws.s3.listObjectsV2Paginated {
            this.bucket = workUploadBuket
            this.prefix = "${workUploadInputDir}${targetSfnId}/"
        }.toList().map { v -> v.contents?.map { it.key!! } ?: emptyList() }.flatten()
    }

    /** 간단 결과 리턴 */
    suspend fun describeExecution(sfnId: String): DescribeExecutionResponse {
        return aws.sfn.describeExecution {
            this.executionArn = aws.awsConfig.sfnConfig.executionArn(stateMachineName, sfnId)
        }
    }

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


