package net.kotlinx.aws.javaSdkv2


import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/** 코틀린 애티르뷰트를 자바 버전으로 변경해준다 */
fun Map<String, aws.sdk.kotlin.services.dynamodb.model.AttributeValue>.toJavaAttributeValue(): Map<String, AttributeValue> {
    return this.entries.associate { e ->
        val javaAttribute = when (val att = e.value) {
            is aws.sdk.kotlin.services.dynamodb.model.AttributeValue.S -> AttributeValue.builder().s(att.asS()).build()
            is aws.sdk.kotlin.services.dynamodb.model.AttributeValue.N -> AttributeValue.builder().n(att.asN()).build()
            else -> throw IllegalArgumentException("지원목록에 추가해주세요 : $att")
        }
        e.key to javaAttribute
    }
}