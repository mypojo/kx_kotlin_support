package net.kotlinx.aws

import aws.sdk.kotlin.services.batch.describeJobs
import aws.sdk.kotlin.services.batch.model.JobDetail
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

/**
 * job 주요 정보는 프로젝트마다 다르고, AWS로 부터 호출하는것도 있다.
 * 따라서 기본 메타데이터를 채우기 위해 factory를 통해서 생성함
 * */
class AwsInfoLoader(

    /** AWS 리소스 접근이 필요함  */
    private val aws: AwsClient1,

    /**
     * CDK 참고해서 하드코딩 입력
     * ex) /aws/ecs/web-dev
     */
    private val logGroupNameWeb: String,
    /**
     * CDK 참고해서 하드코딩 입력
     * ex) xxx-web/sin-web_container-dev/01150f8d44cb4ba29609dd20ba9ff76a
     */
    private val logStreamNameWeb: String,

    ) {

    companion object {

        /** 현재 환경변수가 아니라 배치 ID로부터 로드 */
        fun loadBatch(aws: AwsClient1, batchJobId: String): AwsInfo {
            val jobDetail = runBlocking { aws.batch.describeJobs { this.jobs = listOf(batchJobId) }.jobs!!.firstOrNull() } ?: throw IllegalStateException("잡이 없어요")
            return loadBatch(jobDetail)
        }

        /** jobDetail 을 사용한 업데이트는 외부에서도 호출 가능함 */
        fun loadBatch(jobDetail: JobDetail): AwsInfo {
            val container = jobDetail.container!!
            return AwsInfo(
                AwsInstanceType.BATCH,
                jobDetail.jobId!!,
                container.logConfiguration?.options?.get("awslogs-group")!!,
                container.logStreamName!!,
            )
        }
    }

    private val log = KotlinLogging.logger {}

    /**
     * 환경변수로부터 Aws 메타데이터를 추가로 입력해줌
     * instanceType 에 추가로 각종 설정값을을 로드해야 해서 단일 static 함수로 구성하지 않는다.
     * lazy 가 아닌 매번 생성해야함 (스냅스타트 등의 이슈때문)
     */
    fun load(): AwsInfo {
        return try {
            when (val instanceType = AwsInstanceTypeUtil.INSTANCE_TYPE) {
                AwsInstanceType.LAMBDA -> {
                    //람다의 경우 스냅스타트시 정상적으로 로드되지 못할 수 있다.
                    val lambdaName = System.getenv(AwsInstanceTypeUtil.AWS_LAMBDA_FUNCTION_NAME)
                    AwsInfo(
                        instanceType,
                        lambdaName,
                        System.getenv("AWS_LAMBDA_LOG_GROUP_NAME") ?: "unknown",
                        System.getenv("AWS_LAMBDA_LOG_STREAM_NAME") ?: "unknown",
                    )
                }

                AwsInstanceType.LOCAL -> {
                    AwsInfo(
                        instanceType,
                        "-",
                        "-",
                        "-",
                    )
                }

                AwsInstanceType.CODEBUILD -> {
                    AwsInfo(
                        instanceType,
                        "-",
                        "-",
                        "-",
                    )
                }

                AwsInstanceType.ECS -> {
                    val containerMetadataUri = System.getenv("ECS_CONTAINER_METADATA_URI")
                    val containerId = containerMetadataUri.substringAfterLast("/") //StringFindUtil.getLast(container_metadata_uri, "/")
                    val linkId = containerId.substringBefore("-") //StringFindUtil.getFirst(containerId, "-")
                    AwsInfo(
                        instanceType,
                        "-",
                        logGroupNameWeb,
                        "$logStreamNameWeb/$linkId",
                    )
                }

                AwsInstanceType.BATCH -> {
                    val batchJobId = System.getenv(AwsInstanceTypeUtil.AWS_BATCH_JOB_ID)
                    loadBatch(aws, batchJobId)
                }

            }
        } catch (e: Exception) {
            log.warn { "AwsInfo 생성중 오류!! 환경변수 확인" }
            System.getenv().forEach {
                log.warn { " -> $it" }
            }
            throw e
        }
    }


}
