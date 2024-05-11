package net.kotlinx.domain.job.csv

import java.time.LocalDateTime

class JobLock {


    //==================================================== 분산락 (미구현) ======================================================

    /** 이게 있으면 job 트리거에서 동시 실행 제한함. ex) KK_API  */
    var lockDiv: String? = null

    /** 이게 있으면 job 트리거에서 동시 실행 제한함. ex) 카카오 memberId  */
    var lockKey: String? = null

    /** 락 체크시마다 1회씩 증가.  */
    var lockTryCnt: Int? = null

    /** 락 체크시 타임아웃  */
    var lockTryTimeout: LocalDateTime? = null

    /** 락 대기 (분)  */
    var lockWaitMin: Int? = null


}