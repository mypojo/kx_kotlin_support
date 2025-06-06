package net.kotlinx.domain.item.errorLog

import net.kotlinx.aws.dynamo.enhanced.DbItem
import java.time.LocalDateTime

/**
 * 간단 에러 로그 저장은 이걸로
 */
data class ErrorLog(

    /**
     * 사용할 목적의 group
     * ex) job, task, system ..
     *  */
    val group: String,

    /**
     * 사용할 목적의 div
     * ex) job PK -> xxjob
     *  */
    val div: String,

    /**
     * 구분 ID
     * ex) job SK
     *  */
    val divId: String,

    /**
     * 로그의 유니크 ID
     * ex) UUID
     *  */
    val id: String,

    //==================================================== 내용 ======================================================

    /** TTL. */
    val ttl: Long,

    /** 에러 발생 시간 */
    val time: LocalDateTime,

    /** 에러 문구 */
    val cause: String,

    /** 스택 트레이스 */
    val stackTrace: String,

    ) : DbItem {

    override val pk: String
        get() = ErrorLogConverter.toPk(group, div)

    override val sk: String
        get() = ErrorLogConverter.toSk(divId, id)
}