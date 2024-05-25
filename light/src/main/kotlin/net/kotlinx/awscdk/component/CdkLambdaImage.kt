package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.toCdk
import net.kotlinx.awscdk.util.TagUtil
import net.kotlinx.core.Kdsl
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Size
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType.PRIVATE_WITH_EGRESS
import software.amazon.awscdk.services.ecr.IRepository
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.sqs.IQueue
import kotlin.time.Duration.Companion.minutes

/**
 * 람다 함수 정의 (도커 이미지 버전)
 * 테스트 필요!
 * */
class CdkLambdaImage : CdkEnum {

    @Kdsl
    constructor(block: CdkLambdaImage.() -> Unit = {}) {
        apply(block)
    }

    /** 람다 이름 */
    lateinit var lambdaName: String

    override val logicalName: String
        get() = "${project.projectName}-${lambdaName}-${deploymentType.name.lowercase()}"

    /** 필수 권한 */
    lateinit var role: IRole

    /** VPC */
    var vpc: IVpc? = null

    /** VPC 시큐리티 그룹 */
    var securityGroups: List<ISecurityGroup> = emptyList()

    /**
     * 핸들러 이름
     * ex) net.kotlinx.kx.fn.FunctionHandler
     *  */
    lateinit var handlerName: String

    /** ECR 리파지토리. */
    lateinit var ecrRepository: IRepository

    /** DLQ 를 사용할 경우 입력 */
    var dlq: IQueue? = null

    /** 있으면 네이밍 추가 */
    var aliasName: String? = null

    /** 디폴트로 최대인 15분 */
    var timeout: kotlin.time.Duration = 15.minutes

    /** 최저 보다는 여유있게 줌 */
    var memorySize: Int = 512 * 2

    /** 람다 로그설정 변경시, 별도 람다가 생겨서 보기싫게됨  */
    var logRetention = RetentionDays.SIX_MONTHS

    /**
     * 기본으로 리트라이 안함!  0~2 설정.
     * 비동기일때만 작동 ex) 스케쥴링
     * 약 1~2분 후에 재시도
     * 참고로 스케줄링, 람다 등에서 리트라이 설정 가능 ->  각각 3번씩 리트라이하는 경우 최대 9번 리트라이됨
     *  */
    var retryCnt: Int = 0

    /** 다른 리소스를 보호하기 위한 람다 동시성 최대값 */
    var reservedConcurrentExecutions: Int = if (deploymentType == DeploymentType.PROD) 200 else 10

    /** 스토리지. 기본 최소 사이즈 */
    var ephemeralStorageSize = Size.mebibytes(512)

    /** 결과 (디폴트) */
    lateinit var defaultFun: IFunction

    /** 결과 (네임드) */
    lateinit var aliasFun: IFunction

    fun create(stack: Stack) {

        defaultFun = DockerImageFunction(
            stack, lambdaName, DockerImageFunctionProps.builder()
                .functionName(lambdaName)
                .description("${lambdaName}-$deploymentType")
                .memorySize(memorySize)
                .ephemeralStorageSize(ephemeralStorageSize)
                .timeout(timeout.toCdk())
                .role(role)
                .apply {
                    // VPC가 있을경우 디폴트 설정
                    if (vpc != null) {
                        vpc(vpc)
                        vpcSubnets(SubnetSelection.builder().subnetType(PRIVATE_WITH_EGRESS).build())
                        securityGroups(securityGroups)
                    }
                }
                .code(
                    DockerImageCode.fromEcr(
                        ecrRepository, EcrImageCodeProps.builder()
                            .entrypoint(
                                listOf(
                                    "java", "-cp", "/app/resources:/app/classes:/app/libs/*",
                                    "com.amazonaws.services.lambda.runtime.api.client.AWSLambda"  // 도커의 경우 main(args) 가 있는 람다 런타임.
                                )
                            )
                            .cmd(listOf("${handlerName}::handleRequest"))
                            .tagOrDigest("dev") //최초 생성시 한번만 필요. 다만 벨리데이션 체크 때문에 실제 있는거로 고정으로 해야할듯.. 꼬이면 전체 삭재후재생성. -> 맘에 안듬
                            .build()
                    )
                )
                .logRetention(logRetention)
                .environment(
                    mapOf(
                        DeploymentType::class.simpleName to deploymentType.name
                    )
                )
                .maxEventAge(Duration.hours(6)) //디폴트가 최대치임. 정확히 뭔지?
                .retryAttempts(retryCnt) //로직의 혼란을 막기 위해 리트라이 안함!!!
                .deadLetterQueueEnabled(true)
                .deadLetterQueue(dlq)
                .reservedConcurrentExecutions(reservedConcurrentExecutions) //다른 리소스 보호용. 500개 안에서 분배.
                .build()
        )
        TagUtil.tag(defaultFun, deploymentType)

        aliasName?.let {
            aliasFun = Alias.Builder.create(stack, "lambda-alias-${aliasName}-${deploymentType.name.lowercase()}")
                .aliasName(aliasName)
                .version(defaultFun.latestVersion).build()
        }
    }

}