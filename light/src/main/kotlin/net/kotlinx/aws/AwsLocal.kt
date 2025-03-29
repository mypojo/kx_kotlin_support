package net.kotlinx.aws


object AwsLocal {

    /** 프로파일 없는 로컬 전용 클라이언트 */
    val CLIENT by lazy { AwsConfig().toAwsClient() }

    /**
     * 로그인한 AWS ID
     * ex) ex) arn:aws:iam::xx:user/sin  => sin
     *  */
    val AWS_USER_NAME by lazy { CLIENT.awsConfig.callerIdentity.arn!!.substringAfter("/") }

}