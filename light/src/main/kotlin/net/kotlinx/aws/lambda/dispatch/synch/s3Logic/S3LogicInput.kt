package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kotlinx.json.serial.SerialJsonSet
import net.kotlinx.json.serial.SerialParseJson
import net.kotlinx.json.serial.SerialToJson

/**
 * S3에 JSON 덩어리로 저장되는 입력 데이터
 * */
@Serializable
data class S3LogicInput(
    /** 커스텀 로직 이름 */
    val logicName: String,
    /** 데이터들 */
    val datas: List<String>,
    /** 로직 옵션 */
    val logicOption: String = "{}",
) : SerialToJson {

    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)

    companion object Parse : SerialParseJson {

        override fun parseJson(json: String): S3LogicInput = SerialJsonSet.JSON_OTHER.decodeFromString<S3LogicInput>(json)

    }

}