package net.kotlinx.aws


import mu.KotlinLogging

object AwsInstanceTypeUtil {

    private val log = KotlinLogging.logger {}

    const val AWS_LAMBDA_FUNCTION_NAME = "AWS_LAMBDA_FUNCTION_NAME"
    const val AWS_BATCH_JOB_ID = "AWS_BATCH_JOB_ID"

    private const val AWS_ECS_FARGATE = "AWS_ECS_FARGATE"

    private const val CODEBUILD_BUILD_ID = "CODEBUILD_BUILD_ID"

    private fun doGetInstanceType(): AwsInstanceType {
        if (System.getenv(AWS_LAMBDA_FUNCTION_NAME) != null) return AwsInstanceType.lambda
        if (System.getenv(CODEBUILD_BUILD_ID) != null) return AwsInstanceType.codebuild
        if (isAwsEcsFargate) {
            return if (System.getenv(AWS_BATCH_JOB_ID) == null) AwsInstanceType.ecs else AwsInstanceType.batch
        }
        if (isLocal) return AwsInstanceType.local

        throw IllegalStateException("AWS job 작동환경을 알 수 없습니다. ${System.getenv()}")
    }

    /** instanceType 을 가져온다 */
    val instanceType: AwsInstanceType by lazy { doGetInstanceType().apply { log.info("instanceType => $this") } }

    val isAwsEcsFargate: Boolean by lazy { AWS_ECS_FARGATE == System.getenv("AWS_EXECUTION_ENV") }

    /** 로컬인지?  */
    val isLocal: Boolean by lazy {
        System.getProperty("os.name").let {
            it.contains("Windows", true) || it.contains("Mac", true)
        }
    }


}