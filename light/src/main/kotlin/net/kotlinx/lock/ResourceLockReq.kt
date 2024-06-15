package net.kotlinx.lock

import net.kotlinx.core.Kdsl

class ResourceLockReq {

    @Kdsl
    constructor(block: ResourceLockReq.() -> Unit = {}) {
        apply(block)
    }

    /** 락 이름 */
    lateinit var resourcePk: String

    /** 락 요청 수 */
    var lockCnt: Int = 0

    /**
     * 락 구분값.
     * ex) job 이름
     *  */
    var div: String = ""

    /**
     * 락 사유 등의 코멘트
     *  */
    var cause: String = ""

}