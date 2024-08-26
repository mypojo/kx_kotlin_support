package net.kotlinx.domain.ddb.repeatTask

import net.kotlinx.core.Kdsl

/**
 * 반복되는 작업을 람다에서 빠르게 조회하기 위한 테이블
 */
class RepeatTask {

    @Kdsl
    constructor(block: RepeatTask.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 키값 ======================================================

    /**
     * 사용할 목적의 group
     * ex) job, task, system ..
     *  */
    lateinit var group: String

    /**
     * 사용할 목적의 div
     * ex) xxjob, xxtask ..
     *  */
    lateinit var div: String

    /** 관련 회원 ID  */
    lateinit var memberId: String

    /** 유니크 ID  */
    lateinit var id: String

    //==================================================== 내용 ======================================================

    /** TTL. */
    var ttl: Long? = null

    /**
     * 실행시간
     * ex) 18:22 , MON:18 ...
     *  */
    lateinit var time: String


}