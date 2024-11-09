package net.kotlinx.awscdk.basic

import software.amazon.awscdk.services.events.IRuleTarget
import software.amazon.awscdk.services.events.targets.KinesisFirehoseStream
import software.amazon.awscdk.services.events.targets.KinesisFirehoseStreamProps
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.events.targets.SnsTopic
import software.amazon.awscdk.services.kinesisfirehose.CfnDeliveryStream
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.sns.ITopic


/** 타겟 외우기 싫어서 유틸로 뺌 */
object CdkTargetUtil {

    fun target(target: ITopic): IRuleTarget = SnsTopic(target)
    fun target(target: IFunction): IRuleTarget = LambdaFunction(target)

    /**
     * 대체품이 알파인데 이미 디프리케이션 됨..
     * https://docs.aws.amazon.com/cdk/api/v2/docs/@aws-cdk_aws-kinesisfirehose-alpha.DeliveryStream.html
     * https://mvnrepository.com/artifact/software.amazon.awscdk/kinesisfirehose-alpha/2.165.0-alpha.0
     *
     * 별도의 메세지 변환은 등록하지 않음. 이벤트 브릿지에서 파케이로 잘 변환됨
     * */
    fun target(target: CfnDeliveryStream): IRuleTarget = KinesisFirehoseStream(target, KinesisFirehoseStreamProps.builder().build())

}