package net.kotlinx.awscdk.channel

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.toCdk
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.sqs.IQueue
import software.amazon.awscdk.services.sqs.Queue
import software.amazon.awscdk.services.sqs.QueueProps
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

/** enum */
class CdkSqs(val name: String) : CdkEnum {

    override val logicalName: String
        get() = "${project.profileName}-${name}-${deploymentType.name.lowercase()}"

    /**
     * 메시지가 대기열에서 소비된 후에만 숨겨짐 (큐에서 읽어갔으나 아직 삭제 전)
     * 이 기간은 SQS가 다른 소비 구성 요소가 해당 메시지를 수신 및 처리하는 것을 방지
     * 소비자는 표시 시간 제한 내에 메시지를 처리 후 삭제해야 합니다.
     * 표시 제한 시간이 만료되기 전에 소비자가 메시지를 삭제하지 못하면 다른 소비자가 메시지를 다시 볼 수 있음
     * 각 메시지의 처리 시간을 고려할 만큼 충분히 커야 함
     * 디폴트 30초.  0 ~ 12시간
     * */
    var visibilityTimeout: kotlin.time.Duration = 30.seconds

    /**
     * 대기열에 보내는 모든 메시지는 지연 기간 동안 소비자에게 보이지 않는 상태로 숨겨짐
     * ex) 큐에 들어간 후 10분 뒤에 작동해야함
     *  → 10초, 20초, 30초 등으로 설정된 큐를 한번에 넣으면 각각 10초 딜레이로 작동된다.
     * DDB 등에 입력시, eventually 동기화기 때문에 조금 디레이 줘야 안전하게 읽을 수 있음
     * 기본값 0 / max 15분
     * */
    var deliveryDelay: kotlin.time.Duration = 0.seconds

    /**
     * Amazon SQS가 메시지를 보관하는 시간
     * 1뷴~14일
     * */
    var retentionPeriod: kotlin.time.Duration = 4.days

    var fifo: Boolean = false

    lateinit var iQueue: IQueue

    fun create(stack: Stack, block: QueueProps.Builder.() -> Unit = {}): CdkSqs {
        val props = QueueProps.builder()
            .queueName(this.logicalName)
            .visibilityTimeout(visibilityTimeout.toCdk())  //자주 확인 가능하도록 짧게 줌. 디폴트 30초임
            .deliveryDelay(deliveryDelay.toCdk())
            .retentionPeriod(retentionPeriod.toCdk())
            .apply {
                if (fifo) fifo(true) //여기 false 를 넣으면 에러난다.. 그대로 둬서 null이 되게 해야함. CDK 수준 참..
            }
            .apply(block)
            .build()
        iQueue = Queue(stack, "sqs-$logicalName", props)
        return this
    }

    fun load(stack: Stack): CdkSqs {
        try {
            iQueue = Queue.fromQueueArn(stack, "sqs-$logicalName", "arn:aws:sqs:ap-northeast-2:${project.awsId}:${logicalName}")
        } catch (e: Exception) {
            println(" -> [${stack.stackName}] object already loaded -> $logicalName")
        }
        return this
    }

}