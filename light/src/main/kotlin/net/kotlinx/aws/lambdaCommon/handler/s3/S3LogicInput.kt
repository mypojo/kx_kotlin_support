package net.kotlinx.aws.lambdaCommon.handler.s3

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kotlinx.core.serial.SerialJsonCompanion
import net.kotlinx.core.serial.SerialJsonObj
import net.kotlinx.core.serial.SerialJsonSet

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
) : SerialJsonObj {

    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)

    companion object : SerialJsonCompanion {

        override fun parseJson(json: String): S3LogicInput = SerialJsonSet.JSON_OTHER.decodeFromString<S3LogicInput>(json)

    }

}