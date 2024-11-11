package net.kotlinx.aws.dynamo


/** 권장하는 키값 스타일 마킹 인터페이스 */
@Deprecated("쓰지마셈")
interface DynamoBasic {

    val pk: String
    val sk: String

    companion object {
        const val PK = "pk"
        const val SK = "sk"
    }
}

