package net.kotlinx.awscdk.channel

import mu.KotlinLogging
import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.lazyLoad.default
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.kinesis.IStream
import software.amazon.awscdk.services.kinesis.Stream
import software.amazon.awscdk.services.kinesis.StreamMode

/**
 * */
class CdkKinesis(val name: String) : CdkEnum {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    override val logicalName: String
        get() = "${projectName}-${name}-${suff}"

    var id: String by default { "kinesis-${logicalName}" }

    /** 결과 */
    lateinit var stream: IStream

    fun create(stack: Stack, block: Stream.Builder.() -> Unit = {}): CdkKinesis {
        stream = Stream.Builder.create(stack, id)
            .streamName(logicalName)
            .shardCount(1)
            .streamMode(StreamMode.PROVISIONED)
            .apply(block)
            .build()
        return this
    }

    fun load(stack: Stack): CdkKinesis {
        try {
            stream = Stream.fromStreamArn(stack, id, "arn:aws:kinesis:${awsConfig.region}:${awsConfig.awsId}:stream/${logicalName}")
        } catch (e: Exception) {
            log.info { " -> [${stack.stackName}] object already loaded -> $logicalName" }
        }
        return this
    }

}