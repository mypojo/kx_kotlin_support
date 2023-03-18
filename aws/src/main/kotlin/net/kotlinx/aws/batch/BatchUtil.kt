package net.kotlinx.aws.batch

import net.kotlinx.aws1.AwsConfig

object BatchUtil {


    //==================================================== 내부 예약어 ======================================================

    /** cdk의 JobDefinitionProps.command 와 일치해야함. 편의상 json 하나로 관리 */
    const val MAIN_ARGS_KEY = "JOB_CONFIG"

    /** AWS_BATCH_JOB_ID  */
    const val ENV_ID = "AWS_BATCH_JOB_ID"


    //==================================================== 유틸 ======================================================

    /**
     * 배치 UI 링크를 생성해준다.
     * 리즌은 서울로 고정
     */
    fun toBatchUiLink(jobId: String, region: String = AwsConfig.SEOUL): String = "https://$region.console.aws.amazon.com/batch/v2/home?region=$region#jobs/detail/$jobId"

}