package net.kotlinx.aws1.dynamo


interface DynamoDbBasic{
    val pk: String
    val sk: String

    companion object{
        const val pk = "pk"
        const val sk = "sk"
    }

}



