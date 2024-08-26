package net.kotlinx.domain.ddb

import net.kotlinx.aws.dynamo.DynamoData
import net.kotlinx.core.DataConverter


interface DdbBasicConverter<A : DynamoData, B> : DataConverter<A, B> {

    val pkPrefix: String
    val skPrefix: String

    /** 기본 입력/삭제 등에 사용 */
    fun createBasic(data: B): DdbBasic

}