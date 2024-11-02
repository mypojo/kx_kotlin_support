package net.kotlinx.domain.ddb.errorLog

import net.kotlinx.core.Kdsl
import java.time.LocalDateTime

/**
 * 반복되는 작업을 람다에서 빠르게 조회하기 위한 테이블
 */
class ErrorLog {

    @Kdsl
    constructor(block: ErrorLog.() -> Unit = {}) {
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

    /** 구분 ID  */
    var divId: String? = null

    /** 로그의 유니크 ID  */
    var id: String? = null

    //==================================================== 내용 ======================================================

    /** TTL. */
    var ttl: Long? = null

    /**
     * 에러 발생 시간
     *  */
    var time: LocalDateTime? = null

    /** 에러 문구 */
    var cause: String? = null

    /** 스택 트레이스 */
    var stackTrace: String? = null


}