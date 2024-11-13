package net.kotlinx.aws.javaSdkv2.dynamoLock

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.ddb.DbItem
import net.kotlinx.core.Kdsl

/** 나머지 구현하기 */
class DynamoLockReq : DbItem {

    @Kdsl
    constructor(block: DynamoLockReq.() -> Unit = {}) {
        apply(block)
    }

    override lateinit var pk: String
    override lateinit var sk: String

    //==================================================== 기본 설정값 ======================================================

    /** 커스텀한 타임아웃. null이면 기본값 사용됨 */
    var additionalTimeout: Long? = null

    //==================================================== 추가 DDB 저장 설정값 ======================================================

    /**
     * 락 구분값.
     * ex) job 이름
     *  */
    var div: String = ""

    /**
     * 락 사유 등의 코멘트
     *  */
    var comment: String = ""

    override fun toString(): String {
        return "[${pk}:${sk}] -> (${div}) $comment"
    }

    //==================================================== 입출력 오버라이드 ======================================================

    /** 입력에만 사용됨? */
    fun toAttribute(): Map<String, AttributeValue> {
        return mapOf(
            DynamoLockReq::div.name to AttributeValue.S(this.div),
            DynamoLockReq::comment.name to AttributeValue.S(this.comment),
        )
    }

//    /** 아무것도 하지않음 */
//    fun <T : DbItem> fromAttributeMap(map: Map<String, AttributeValue>): T {
//        return DynamoLockReq {
//            pk = map.findOrThrow(DynamoLockReq::pk)
//            sk = map.findOrThrow(DynamoLockReq::sk)
//            div = map.findOrThrow(DynamoLockReq::div)
//            comment = map.findOrThrow(DynamoLockReq::comment)
//        } as T
//    }
}