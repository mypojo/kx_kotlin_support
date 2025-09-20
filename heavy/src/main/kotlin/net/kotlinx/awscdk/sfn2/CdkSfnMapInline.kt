package net.kotlinx.awscdk.sfn2

import net.kotlinx.aws.AwsNaming
import software.amazon.awscdk.services.stepfunctions.*
import software.amazon.awscdk.services.stepfunctions.Map
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke


/**
 * SFN MAP 작업 : S3 list -> lambda
 * 병렬 작업하는애들 개별 상태관리 하지 않고, 인라인으로 관리함 (저렴)
 *
 * 아래 설정 참고
 * https://docs.aws.amazon.com/ko_kr/step-functions/latest/dg/state-map-distributed.html
 * */
class CdkSfnMapInline(
    override val sfn: CdkSfn,
    override val id: String,
    override val stateName: String,
) : CdkSfnChain {


    /** 스탭 내부의 Id */
    var stepId: String = "${id}_Inline"

    /** 스탭 내부의 state 이름 */
    var stepName: String = "${stateName}_Inline"

    /** itemsPath */
    var itemPath: String? = null

    /** S3 경로로 상품정보 입력 (DISTRIBUTED 전용) */
    var itemReader: S3ObjectsItemReader2? = null

    var resultPath: String = "$.${AwsNaming.OPTION}.${id}"

    /**
     * maxConcurrency 하드설정 안쓰고 사용자 입력으로 사용함!
     * DISTRIBUTED 모드는 람다 리밋까지 지원
     * INLINE 모드는 최대 40개인듯
     * 참고! 이 설정은 UI에서는 불가능함.. 에반데..
     * */
    var maxConcurrencyPath: String = "$.${AwsNaming.OPTION}.maxConcurrency"

    /** 처리기 */
    var processorConfig = INLINE


    //==================================================== 오류 3종 처리 ======================================================

    var retryIntervalSeconds: Int = 10 // 적게 주어야 더 빠르게 작동할듯
    var backoffRate: Double = 1.2 //오류시 리트라이 증분. IP 블록 우회하는 크롤링이라면 동시에 실행되어야 람다가 다르게 실생되서 분산된다.
    var maxAttempts: Int = 3

    /** 별도 설정이 없어서 노가다 했음.. 차라리 이게 더 나은듯.. */
    override fun convert(): State {

        val lambdaTask = LambdaInvoke.Builder.create(sfn.stack, "${sfn.logicalName}-${stepId}")
            .lambdaFunction(sfn.lambda)
            .stateName(stepName)
            .outputPath("$.Payload") //고정
            .payload(TaskInput.fromJsonPathAt("$"))
            .retryOnServiceExceptions(true)
            .build()

        val retryProps = RetryProps.builder()
            .errors(
                listOf(
                    "Lambda.ServiceException",
                    "Lambda.AWSLambdaException",
                    "Lambda.SdkClientException",
                    "Lambda.TooManyRequestsException",
                    "States.TaskFailed"
                )
            )
            .interval(software.amazon.awscdk.Duration.seconds(retryIntervalSeconds))
            .backoffRate(backoffRate)
            .maxAttempts(maxAttempts)
            .build()

        /** 생성자가 틀림.. 미묘하게 하드코딩 되어있어서 그대로 쓴다 */
        return when (processorConfig.mode) {
            ProcessorMode.INLINE -> {
                val map = Map.Builder.create(sfn.stack, "${sfn.logicalName}-${id}")
                    .stateName(stateName)
                    .maxConcurrencyPath(maxConcurrencyPath)
                    .resultPath(resultPath)
                    .apply {
                        itemPath?.let { itemsPath(it) }
                    }
                    .build()!!
                val peoc = map.itemProcessor(lambdaTask, processorConfig)
                peoc.addRetry(retryProps)
            }

            ProcessorMode.DISTRIBUTED -> {
                val map = DistributedMap.Builder.create(sfn.stack, "${sfn.logicalName}-${id}")
                    .stateName(stateName)
                    .maxConcurrencyPath(maxConcurrencyPath)
                    .resultPath(resultPath)
                    .mapExecutionType(StateMachineType.STANDARD)
                    .apply {
                        itemPath?.let { itemsPath(it) }
                        itemReader?.let { itemReader(itemReader) } //둘다 있으면 이게 우선함
                    }.build()!!

                val peoc = map.itemProcessor(lambdaTask) //DISTRIBUTED 타입은 컨피그를 다르게 설정함
                peoc.addRetry(retryProps)
            }

            else -> throw IllegalArgumentException("지원하지 않는 모드입니다. $processorConfig")
        }
    }

    companion object {

        /**
         * 간단한거만. 최대 40개 병렬처리
         * ex) 크롤링, 요청 완료된건 결과처리
         *  */
        val INLINE = ProcessorConfig.builder()
            .executionType(ProcessorType.STANDARD)
            .mode(ProcessorMode.INLINE)
            .build()!!

        /**
         * 복잡하고 , 컨텍스트가 큰거
         * ex) 대량 리포트 처리
         * */
        val DISTRIBUTED = ProcessorConfig.builder()
            .executionType(ProcessorType.STANDARD)
            .mode(ProcessorMode.DISTRIBUTED)
            .build()!!

    }

}