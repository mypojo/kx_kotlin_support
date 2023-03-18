package net.kotlinx.aws.ecr

object EcrUtil {

    /**
     * ECR 경로
     * ex) 331671628331.dkr.ecr.ap-northeast-2.amazonaws.com/media-data:latest
     * ex) 331671628331.dkr.ecr.ap-northeast-2.amazonaws.com/media-data@sha256:24c4d31fc292a57f32ffcd4d2719f0b10bfea9d08786af589196547af5bb960f
     */
    fun toEcrPath(awsId: String, repositoryName: String, version: String = "latest"): String = "$awsId.dkr.ecr.ap-northeast-2.amazonaws.com/$repositoryName:$version"
}