package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.s3.IBucket
import software.amazon.awscdk.services.sqs.IQueue

/**
 * 람다 함수 정의 (일반 버전)
 * */
class CdkLambda(
    val project: CdkProject,
    val name: String = "fn",
    block: CdkLambda.() -> Unit = {},
) : CdkInterface {

    var deploymentType: DeploymentType = DeploymentType.DEV

    override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType.name.lowercase()}"

    /** 필수 권한 */
    lateinit var role: IRole

    /**
     * 핸들러 이름
     * ex) net.kotlinx.kx.fn.FunctionHandler
     *  */
    lateinit var handlerName: String

    /**
     * 최초 업로드할 아무 자르
     * 아무거나 지정 (보통 여기서 업데이트 안함)
     * ex) Code.fromAsset("src/main/resources/function-latest.jar")
     *  */
    lateinit var code: Code

    /** DLQ 를 사용할 경우 입력 */
    var dlq: IQueue? = null

    /**
     * 레이어 소스코드 정보. 있으면 추가함
     * JVM 레이어는 용량이 크기때문에 S3 권장!
     * 본문 함수가 50mb 이상이면 레이어로 빼야 직접 업데이트가 가능함
     * 레이어 jar는 CDK 실행전에 여기 미리 파일을 업로드 해놓아야함 (gradle 사용 추천)
     *  */
    var layerCode: Pair<IBucket, String>? = null

    /** 런타임 */
    var runtime: Runtime = Runtime.JAVA_17!!

    /** 스냅스타트를 사용할것인지 */
    var snapstart: Boolean = false

    /** 있으면 네이밍 추가 */
    var aliasName: String? = null

    /** 디폴트로 최대인 15분 */
    var timeout = Duration.seconds(60 * 15)

    /** 보통 이게 최저 */
    var memorySize = 256

    /** 람다 로그설정 변경시, 별도 람다가 생겨서 보기싫게됨  */
    var logRetention = RetentionDays.SIX_MONTHS

    init {
        block(this)
    }


    /** 결과 레이어 */
    var layer: LayerVersion? = null

    /** 결과 (디폴트) */
    lateinit var defaultFun: Function

    /** 결과 (네임드) */
    lateinit var aliasFun: Alias

    fun create(stack: Stack) {

        layerCode?.let {
            layer = LayerVersion(
                stack, "layer-$logicalName", LayerVersionProps.builder()
                    .layerVersionName("layer-$logicalName")
                    .code(Code.fromBucket(it.first, it.second)) //
                    .compatibleRuntimes(listOf(runtime)) //17로 지정해도 11로 표기됨..  의미 없을듯
                    .build()
            )
        }

        defaultFun = Function(
            stack, logicalName, FunctionProps.builder()
                .functionName(logicalName)
                .runtime(runtime)
                .role(role)
                .memorySize(memorySize)
                .timeout(timeout)
                .handler(handlerName)
                .logRetention(RetentionDays.SIX_MONTHS)
                .code(code)
                .environment(
                    mapOf(
                        DeploymentType::class.simpleName to deploymentType.name //여기는 또 map이다.. MSA의 단점을 이렇게 보여주는듯.
                    )
                )
                .apply {
                    layer?.let { layers(listOf(it)) }
                    dlq?.let { deadLetterQueue(it) }
                    if (!snapstart) {
                        architecture(Architecture.ARM_64) //ARM이 더 쌈. 하지만 SnapStart를 지원하지 않음 ㅠㅠ
                    }
                }
                .build()
        )
        TagUtil.tag(defaultFun, deploymentType)

        if (snapstart) {
            //스냅스타트 온
            val cfnFunction = defaultFun.node.defaultChild as CfnFunction
            cfnFunction.addPropertyOverride("SnapStart", mapOf("ApplyOn" to "PublishedVersions"))
        }

        aliasName.let {
            aliasFun = Alias.Builder.create(stack, "lambda-alias-${aliasName}-${deploymentType.name.lowercase()}")
                .aliasName(aliasName)
                .version(defaultFun.latestVersion).build()
        }
    }

}