package net.kotlinx.awscdk.lambda

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Size
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
class CdkLambda : CdkEnum {

    @Kdsl
    constructor(name: String, block: CdkLambda.() -> Unit = {}) {
        apply(block).apply { this.lambdaName = name }
    }

    /** 람다 이름 */
    lateinit var lambdaName: String

    override val logicalName: String
        get() = "${project.profileName}-${lambdaName}-${suff}"

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
    var runtime: Runtime = Runtime.JAVA_21!!

    /** 스냅스타트를 사용할것인지 */
    var snapstart: Boolean = false

    /** 있으면 네이밍 추가 */
    var aliasName: String? = null

    /** 디폴트로 최대인 15분 */
    var timeout: kotlin.time.Duration = 15.minutes

    /** 보통 이게 최저 */
    var memorySize = 256

    /**
     * 기본값은 무료. 초과시 초당 비용 받음
     * 참고로 스냅스타트 사용시 용량조절 불가능함!
     *  */
    var ephemeralStorageSize = 512

    /** 람다 로그설정 변경시, 별도 람다가 생겨서 보기싫게됨  */
    var logRetention = RetentionDays.SIX_MONTHS

    /**
     * 기본으로 리트라이 안함!  0~2 설정.
     * 비동기일때만 작동 ex) 스케쥴링
     * 약 1~2분 후에 재시도
     * 참고로 스케줄링, 람다 등에서 리트라이 설정 가능 ->  각각 3번씩 리트라이하는 경우 최대 9번 리트라이됨
     *  */
    var retryCnt: Int = 0

    /**
     * 환경변수. 여기에 더 추가할것
     * ex) += Spring.ENV_PROFILE to "default,dev"
     *  */
    var environment: Map<String, String> = mapOf(
        DeploymentType::class.simpleName!! to deploymentType.name
    )

    /** 결과 레이어 */
    var layers: List<ILayerVersion> = emptyList()

    /** 결과 (디폴트) */
    lateinit var defaultFun: IFunction

    /** 결과 (네임드) */
    lateinit var aliasFun: IFunction

    /** 설명 */
    var desc: String = ""


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

    fun create(stack: Stack, block: FunctionProps.Builder.() -> Unit = {}) {

        defaultFun = Function(
            stack, logicalName, FunctionProps.builder()
                .functionName(logicalName)
                .runtime(runtime)
                .role(role)
                .memorySize(memorySize)
                .ephemeralStorageSize(Size.mebibytes(ephemeralStorageSize))
                .timeout(Duration.seconds(timeout.inWholeSeconds)) //초단위로 입력
                .handler(handlerName)
                .logRetention(logRetention)
                .code(code)
                .environment(environment)
                .retryAttempts(retryCnt)
                //.maxEventAge()  //동시성이 충분하지 못할경우 대기시간.. 필요없음
                .apply {
                    if (layers.isNotEmpty()) {
                        layers(layers)
                    }
                    dlq?.let { deadLetterQueue(it) }
                    if (!snapstart) {
                        architecture(Architecture.ARM_64) //ARM이 더 쌈. 하지만 SnapStart를 지원하지 않음 ㅠㅠ
                    }
                }
                .description(desc)
                .apply(block)
                .build()
        )

        TagUtil.tag(defaultFun, deploymentType)

        if (snapstart) {
            //스냅스타트 온
            val cfnFunction = defaultFun.node.defaultChild as CfnFunction
            cfnFunction.addPropertyOverride("SnapStart", mapOf("ApplyOn" to "PublishedVersions"))
        }

        aliasName?.let {
            aliasFun = Alias.Builder.create(stack, "lambda-alias-${aliasName}-${suff}")
                .aliasName(aliasName)
                .version(defaultFun.latestVersion).build()
        }
    }

}