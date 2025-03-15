package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import kotlinx.serialization.Serializable
import net.kotlinx.json.serial.SerialJsonSet
import net.kotlinx.json.serial.SerialParseJson
import net.kotlinx.json.serial.SerialToJson

/**
 * S3에 JSON 덩어리로 저장되는 입력 데이터
 * */
@Serializable
data class S3LogicInput(
    /**
     * 커스텀 로직 이름
     * 보통 런타임 클래스의 name()
     * ex) BatchTaskExecutor::class.name()
     *  */
    val logicId: String,
    /**
     * 데이터들.
     * 일단 텍스트 덩어리로 입력함 (각 로직에서 파싱)
     * 일반문자, csv, json 등등 다양한 가능성이 있음 -> 이때문에 가능하면 json 으로 입력
     *  */
    val datas: List<String>,
    /** 로직 옵션 */
    val logicOption: String = "{}",
) : SerialToJson {

    /** 이거 자체를 그대로 json화 한다 */
    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)

    companion object Parse : SerialParseJson {

        override fun parseJson(json: String): S3LogicInput = SerialJsonSet.JSON_OTHER.decodeFromString<S3LogicInput>(json)

    }

}