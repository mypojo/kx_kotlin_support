package net.kotlinx.awscdk

import net.kotlinx.aws.AwsConfig
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps


/** CDK 버전의 Duration 으로 변경해준다.  */
fun kotlin.time.Duration.toCdk(): Duration = Duration.millis(this.inWholeMilliseconds)

/** 간단 props 리턴.  리즌 변경 버전이 필요할때 있음 주의!  */
fun AwsConfig.toProps(): StackProps {
    val environment = Environment.builder().account(awsId).region(region).build()
    return StackProps.builder().env(environment).build()
}