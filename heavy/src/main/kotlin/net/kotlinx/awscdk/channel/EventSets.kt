package net.kotlinx.awscdk.channel


/** 이벤트브릿지 모음 */
object EventSets {


    /**
     * https://docs.aws.amazon.com/dtconsole/latest/userguide/concepts.html
     * */
    object CodekPipeline {
        /** 실패 */
        const val FAILED: String = "codepipeline-pipeline-pipeline-execution-failed"

        /** 시작 : 실서버용 */
        const val STARTED: String = "codepipeline-pipeline-pipeline-execution-started"

        /** 성공 : 실서버용 */
        const val SUCCESSED: String = "codepipeline-pipeline-pipeline-execution-succeeded"
    }


}
