package net.kotlinx.module.dynamoLock

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoData

/** 나머지 구현하기 */
open class DynamoLockReq : DynamoData {

    override lateinit var pk: String
    override lateinit var sk: String

    //==================================================== 디폴트설정 ======================================================

    /** 테이블명 사용안함 */
    override val tableName: String = ""

    /** 아무것도 하지않음 */
    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = throw UnsupportedOperationException()

    //==================================================== 기본 설정값 ======================================================

    /** 커스텀한 타임아웃. null이면 기본값 사용됨 */
    var timeout: Long? = null

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

    /** 추가 애트리뷰트 임으로, 키값은 없어도됨 */
    override fun toAttribute(): Map<String, AttributeValue> {
        return mapOf(
            DynamoLockReq::div.name to AttributeValue.S(this.div),
            DynamoLockReq::comment.name to AttributeValue.S(this.comment),
        )
    }


}