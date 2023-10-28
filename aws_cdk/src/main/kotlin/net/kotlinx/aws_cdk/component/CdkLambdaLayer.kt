package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.LayerVersion
import software.amazon.awscdk.services.lambda.LayerVersionProps
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.s3.IBucket

/**
 * 람다 함수 레이어
 * JVM 레이어는 용량이 크기때문에 S3 권장!
 * 본문 함수가 50mb 이상이면 레이어로 빼야 직접 업데이트가 가능함
 * 레이어 jar는 CDK 실행전에 여기 미리 파일을 업로드 해놓아야함 (gradle 사용 추천)
 * */
class CdkLambdaLayer(
    val project: CdkProject,
    val name: String,
    block: CdkLambdaLayer.() -> Unit = {},
) : CdkInterface {

    var deploymentType: DeploymentType = DeploymentType.DEV

    override val logicalName: String
        get() = "${project.projectName}-layer_${name}-${deploymentType.name.lowercase()}"

    /** 런타임 */
    var runtime: Runtime = Runtime.JAVA_17!!

    /** 코드 버킷 */
    lateinit var bucket: IBucket

    /** 코드 키 */
    lateinit var key: String

    init {
        block(this)
    }

    /** 결과 레이어 */
    lateinit var layer: LayerVersion

    fun create(stack: Stack) {
        layer = LayerVersion(
            stack, logicalName, LayerVersionProps.builder()
                .layerVersionName(logicalName)
                .code(Code.fromBucket(bucket, key)) //S3 권장
                .compatibleRuntimes(listOf(runtime)) //17로 지정해도 11로 표기됨..  의미 없을듯
                .build()
        )
    }

}