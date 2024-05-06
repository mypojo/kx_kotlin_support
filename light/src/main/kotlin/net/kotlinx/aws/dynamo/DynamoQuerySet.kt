package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue


/**
 * 자주 사용되는 쿼리 모음
 * */
object DynamoQuerySet {

    val KeyEqualTo = DynamoQuery {
        createParamAndQuery = { mapOf(":${DynamoDbBasic.PK}" to AttributeValue.S(it.pk)) }
    }
    val SortBeginsWith = DynamoQuery {
        createParamAndQuery = {
            mapOf(
                ":${DynamoDbBasic.PK}" to AttributeValue.S(it.pk),
                ":${DynamoDbBasic.SK}" to AttributeValue.S(it.sk),
            )
        }
    }


}