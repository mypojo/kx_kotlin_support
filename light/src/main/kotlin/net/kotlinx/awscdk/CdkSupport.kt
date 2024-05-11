package net.kotlinx.awscdk

import software.amazon.awscdk.Duration


/** CDK 버전의 Duration 으로 변경해준다.  */
fun kotlin.time.Duration.toCdk(): Duration = Duration.millis(this.inWholeMilliseconds)