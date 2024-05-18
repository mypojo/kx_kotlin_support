package net.kotlinx.aws.ecr

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.sts.StsUtil
import net.kotlinx.koin.Koins.koin

object EcrUtil {

    /**
     * ECR 경로
     * 이 위에 버전 정보가 붙을 수 있다.
     * ex) 331671628331.dkr.ecr.ap-northeast-2.amazonaws.com/media-data:latest
     * ex) 331671628331.dkr.ecr.ap-northeast-2.amazonaws.com/media-data@sha256:24c4d31fc292a57f32ffcd4d2719f0b10bfea9d08786af589196547af5bb960f
     */
    fun path(repositoryName: String, region: String = koin<AwsConfig>().region): String {
        return "${StsUtil.ACCOUNT_ID}.dkr.ecr.${region}.amazonaws.com/${repositoryName}"
    }
}