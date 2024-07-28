package net.kotlinx.aws.batch

import net.kotlinx.aws.AwsConfig

object BatchUtil {


    //==================================================== args 전달값 (cdk의 JobDefinitionProps.command 와 일치) ->  ======================================================

    /**
     * job 객체필요한 json 문자열 (jobPk 등)
     * ex) DDB 입력시 pk,sk만 입력
     * ex) sfn 입력시 pk만 입력
     * */
    const val BATCH_ARGS01 = "jobOption01"

//    /**
//     * 잡 옵션 -> jobOption01 과 머지됨.
//     * ex) sfn에서 AWS BATCH 싦행시 동적으로 옵션 입력으로 사용
//     * */
//    const val BATCH_ARGS02 = "jobOption02"

    //==================================================== 내부 예약어 ======================================================

    /** AWS_BATCH_JOB_ID  */
    const val ENV_ID = "AWS_BATCH_JOB_ID"


    //==================================================== 유틸 ======================================================

    /**
     * 배치 UI 링크를 생성해준다.
     * 리즌은 서울로 고정
     */
    fun toBatchUiLink(jobId: String, region: String = AwsConfig.REGION_KR): String = "https://$region.console.aws.amazon.com/batch/v2/home?region=$region#jobs/detail/$jobId"

}