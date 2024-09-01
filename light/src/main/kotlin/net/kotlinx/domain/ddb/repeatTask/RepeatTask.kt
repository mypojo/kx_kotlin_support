package net.kotlinx.domain.ddb.repeatTask

import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData

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
    var group: String? = null

    /**
     * 사용할 목적의 div
     * ex) xxjob, xxtask ..
     *  */
    var div: String? = null

    /** 관련 회원 ID  */
    var memberId: String? = null

    /** 유니크 ID  */
    var id: String? = null

    //==================================================== 내용 ======================================================

    /** TTL. */
    var ttl: Long? = null

    /**
     * 실행시간
     * ex) 18:22 , MON:18 ...
     *  */
    var time: String? = null

    /** 기타등등 입력 */
    var body: GsonData = GsonData.empty()


}