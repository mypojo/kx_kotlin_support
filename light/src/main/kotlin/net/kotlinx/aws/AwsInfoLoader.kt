package net.kotlinx.aws

import aws.sdk.kotlin.services.batch.describeJobs
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * job 주요 정보는 프로젝트마다 다르고, AWS로 부터 호출하는것도 있다.
 * 따라서 기본 메타데이터를 채우기 위해 factory를 통해서 생성함
 * */
class AwsInfoLoader(block: AwsInfoLoader.() -> Unit = {}) : KoinComponent {

    /**
     * ECS 로그를 위한 설정정보
     * CDK 참고해서 하드코딩 입력하면 됨
     * logGroupNameWeb first ex) /aws/ecs/web-dev
     * logStreamNameWeb second ex) xxx-web/sin-web_container-dev/01150f8d44cb4ba29609dd20ba9ff76a
     * */
    lateinit var ecsLogConfig: Pair<String, String>

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
                    //https://docs.aws.amazon.com/ko_kr/lambda/latest/dg/configuration-envvars.html
                    //람다의 경우 스냅스타트시 로그 정보를 제공하지 않음
                    val lambdaName = System.getenv(AwsInstanceTypeUtil.AWS_LAMBDA_FUNCTION_NAME)
                    AwsInfo(
                        instanceType,
                        lambdaName,
                        System.getenv("AWS_LAMBDA_LOG_GROUP_NAME") ?: "/aws/lambda/${lambdaName}",
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

                    check(this::ecsLogConfig.isInitialized) { "ECS의 경우 ecsLogConfig가 등록되어야 합니다" }

                    val containerMetadataUri = System.getenv("ECS_CONTAINER_METADATA_URI")
                    val containerId = containerMetadataUri.substringAfterLast("/") //StringFindUtil.getLast(container_metadata_uri, "/")
                    val linkId = containerId.substringBefore("-") //StringFindUtil.getFirst(containerId, "-")
                    AwsInfo(
                        instanceType,
                        "-",
                        ecsLogConfig.first,
                        "${ecsLogConfig.second}/$linkId",
                    )
                }

                /** AWS 접근이 필요함!! */
                AwsInstanceType.BATCH -> {
                    val batchJobId = System.getenv(AwsInstanceTypeUtil.AWS_BATCH_JOB_ID)!!

                    val aws1: AwsClient1 by inject()
                    val jobDetail = runBlocking { aws1.batch.describeJobs { this.jobs = listOf(batchJobId) }.jobs!!.firstOrNull() } ?: throw IllegalStateException("잡이 없어요")
                    val container = jobDetail.container!!
                    return AwsInfo(
                        AwsInstanceType.BATCH,
                        jobDetail.jobId!!,
                        container.logConfiguration?.options?.get("awslogs-group")!!,
                        container.logStreamName!!,
                    )
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


    init {
        block(this)
    }

}
