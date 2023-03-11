package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.services.events.EventPattern


object EventPatternUtil {

    /**
     * 자주 사용되는거 2개만
     * ex) source: ['project'] / detailType: ['web'],
     *  */
    fun of(source: List<String>, detailType: List<String> = emptyList()): EventPattern = EventPattern.builder().source(source).detailType(detailType).build()


}