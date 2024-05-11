package net.kotlinx.domain.job.csv

/**
 *  미구현..
 */
class JobLineContext {


    //==================================================== stream (csv 업로드) 옵션 입력값 ======================================================
    /** JOB 요청 파일 경로 (s3)  ex)   /aaa/bbb/{id}  */
    var reqFilePath: String? = null

    /** JOB 요청 파일 이름 (원본이름) ex) 4월전반기실적.csv  */
    var reqFileName: String? = null

    //==================================================== stream (csv 업로드) 시스템 자동(필수) 입력값 ======================================================
    /** JOB 요청 파일 크기  */
    var reqFileSize: String? = null

    /** JOB 전체 줄 수  */
    var rowTotalCnt: Long? = null

    /** JOB 성공 줄 수. atomic update  */
    var rowSuccessCnt: Long? = null

    /** JOB 실패 줄 수, atomic update  */
    var rowFailCnt: Long? = null

    /** JOB 결과 파일 경로 (s3)  */
    var resultFilePath: String? = null

    // 별도 프로그레스 모듈 참고!
    //    fun toProcessRate(): BigDecimal {
//        if (!ObjectUtils.allNotNull(rowSuccessCnt, rowTotalCnt, rowFailCnt, startTime)) {
//            return BigDecimal.ZERO
//        }
//        val exeCnt = rowSuccessCnt!! + rowFailCnt!!
//        return DecimalUtil.rate(exeCnt, rowTotalCnt, 2)
//    }
//
//    fun toEstimate(): Long? {
//        if (!ObjectUtils.allNotNull(rowSuccessCnt, rowTotalCnt, rowFailCnt, startTime)) {
//            return null
//        }
//        val interval = TimeUtil.interval(startTime!!.toLocalDateTime(), LocalDateTime.now())
//        val exeCnt = rowSuccessCnt!! + rowFailCnt!!
//        val remainCnt = rowTotalCnt!! - exeCnt
//        return (1.0 * interval / exeCnt * remainCnt).toLong()
//    }

}