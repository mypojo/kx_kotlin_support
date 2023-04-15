package net.kotlinx.aws.batch

import net.kotlinx.aws1.AwsConfig

object BatchUtil {


    //==================================================== args 전달값 (cdk의 JobDefinitionProps.command 와 일치) ->  ======================================================

    /** job 객체필요한 json 문자열 (jobPk 등) */
    const val BATCH_ARGS01 = "jobOption01"
    /** 동적입력 - 잡 옵션 (sfnID 포함) */
    const val BATCH_ARGS02 = "jobOption02"

    //==================================================== 내부 예약어 ======================================================



    /** AWS_BATCH_JOB_ID  */
    const val ENV_ID = "AWS_BATCH_JOB_ID"


    //==================================================== 유틸 ======================================================

    /**
     * 배치 UI 링크를 생성해준다.
     * 리즌은 서울로 고정
     */
    fun toBatchUiLink(jobId: String, region: String = AwsConfig.SEOUL): String = "https://$region.console.aws.amazon.com/batch/v2/home?region=$region#jobs/detail/$jobId"

}