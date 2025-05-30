package net.kotlinx.aws.schedule

import net.kotlinx.aws.AwsConfig
import net.kotlinx.koin.Koins.koin


object AwsScheduler {

    /** 스케쥴링 링크 */
    fun toLink(group: String, name: String, region: String = koin<AwsConfig>().region): String =
        "https://${region}.console.aws.amazon.com/scheduler/home?region=${region}#schedules/${group}/${name}"


}
