package net.kotlinx.lock

import net.kotlinx.aws.ddb.DbItem
import net.kotlinx.json.gson.GsonData

/** 락 잡힌 리소스 */
class ResourceItem(override val pk: String, override val sk: String) : DbItem {

    /** 락이 사용중인지 여부 */
    var inUse: Boolean = false

    /**
     * TTL
     * 보통 작업에 2시간 걸린다면
     * 실제 TTL에서 2시간을 빼고 입력해야 안전하게 리소스 사용이 가능하다.
     *  */
    var ttl: Long = 0

    /** 전달할 내용 */
    lateinit var body: GsonData

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