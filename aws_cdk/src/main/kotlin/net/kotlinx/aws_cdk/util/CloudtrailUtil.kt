package net.kotlinx.aws_cdk.util

import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.cloudtrail.ReadWriteType
import software.amazon.awscdk.services.cloudtrail.Trail
import software.amazon.awscdk.services.cloudtrail.TrailProps
import software.amazon.awscdk.services.s3.IBucket


object CloudtrailUtil {

    /** 간단하게 이력을 남길 기본 트레일. 최초 1개는 공짜라고 한다. */
    fun defaultTrailOn(stack: Stack, bucket: IBucket, trailName: String = "default-cloudtrail", s3KeyPrefix: String = "cloudtrail") {
        val trail = Trail(
            stack, trailName, TrailProps.builder()
                .trailName(trailName)
                .bucket(bucket)
                .s3KeyPrefix(s3KeyPrefix)
                .managementEvents(ReadWriteType.ALL) //첫 트레일은 전부
                .enableFileValidation(false)
                .build()
        )
        //trail.addEventSelector() 1개는 공짜임으로 필터링 일단 무시..
        TagUtil.tag(trail, DeploymentType.PROD) //실서버로 간주

    }


}