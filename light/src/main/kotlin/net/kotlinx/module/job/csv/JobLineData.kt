package net.kotlinx.module.job.csv

import net.kotlinx.module.job.Job

/**
 * 스트림 처리시 csv 파일당 1개 생성
 * 파일의 1개 라인 = JobReqLineData
 */
class JobLineData {

    /** 라인 넘버(0부터)  */
    var lineNumber: Long = 0

    /** itemReader에서 판단한다.  */
    var readSuccess = false

    /** itemProcessor or itemWriter에서 판단한다.  */
    var processSuccess = false

    /** 결과 메세지  */
    var msgs: MutableList<String>? = null

    /** 작업 요청 정보  */
    var job: Job? = null

    /** 매핑된 vo  */
    var result: Any? = null

}