package net.kotlinx.aws.dynamo.multiIndex

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue

/** PK / SK 입력 */
fun MutableMap<String, AttributeValue>.add(index: DbMultiIndex, pair: Pair<String, String>?) {
    pair?.let {
        this += index.pkName to AttributeValue.S(it.first)
        this += index.skName to AttributeValue.S(it.second)
    }
}

/** PK / SK 로드 */
fun Map<String, AttributeValue>.findPair(index: DbMultiIndex): Pair<String, String>? {
    val pk = this[index.pkName]?.asSOrNull()
    val sk = this[index.skName]?.asSOrNull()
    return if (pk == null || sk == null) return null else pk to sk
}

