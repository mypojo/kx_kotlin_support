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
import software.amazon.awscdk.services.sqs.IQueue
import kotlin.time.Duration.Companion.minutes

/**
 * 함수에 URL 오픈
 * 각 함수별로 다를 수 있으니 별도 지정해서 사용
 *  */
fun IFunction.url(type: FunctionUrlAuthType = FunctionUrlAuthType.NONE) {
    this.addFunctionUrl(FunctionUrlOptions.builder().authType(type).build()) //외부 오픈됨. 알리아스 & 버전 없음!
}

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

    /** 런타임 */
    var runtime: Runtime = Runtime.JAVA_17!!

    /** 스냅스타트를 사용할것인지 */
    var snapstart: Boolean = false

    /** 있으면 네이밍 추가 */
    var aliasName: String? = null

    /** 디폴트로 최대인 15분 */
    var timeout: kotlin.time.Duration = 15.minutes

    /** 보통 이게 최저 */
    var memorySize = 256

    /** 람다 로그설정 변경시, 별도 람다가 생겨서 보기싫게됨  */
    var logRetention = RetentionDays.SIX_MONTHS

    /** 결과 레이어 */
    var layers: List<ILayerVersion> = emptyList()

    /** 결과 (디폴트) */
    lateinit var defaultFun: IFunction

    /** 결과 (네임드) */
    lateinit var aliasFun: IFunction


    /** 일반 로드 */
    fun load(stack: Stack): CdkLambda {
        defaultFun = Function.fromFunctionName(stack, logicalName, logicalName)
        return this
    }

    /** alias 버전은 ARN 으로 로드한다. */
    fun loadAlias(stack: Stack): CdkLambda {
        checkNotNull(aliasName)
        val arn = "arn:aws:lambda:${project.region}:${project.awsId}:function:${logicalName}:${aliasName}"
        aliasFun = Function.fromFunctionArn(stack, arn, arn)
        return this
    }

    fun create(stack: Stack) {

        defaultFun = Function(
            stack, logicalName, FunctionProps.builder()
                .functionName(logicalName)
                .runtime(runtime)
                .role(role)
                .memorySize(memorySize)
                .timeout(Duration.seconds(timeout.inWholeSeconds)) //초단위로 입력
                .handler(handlerName)
                .logRetention(logRetention)
                .code(code)
                .environment(
                    mapOf(
                        DeploymentType::class.simpleName to deploymentType.name //여기는 또 map이다.. MSA의 단점을 이렇게 보여주는듯.
                    )
                )
                .apply {
                    if (layers.isNotEmpty()) {
                        layers(layers)
                    }
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

        aliasName?.let {
            aliasFun = Alias.Builder.create(stack, "lambda-alias-${aliasName}-${deploymentType.name.lowercase()}")
                .aliasName(aliasName)
                .version(defaultFun.latestVersion).build()
        }
    }

    init {
        block(this)
    }

}