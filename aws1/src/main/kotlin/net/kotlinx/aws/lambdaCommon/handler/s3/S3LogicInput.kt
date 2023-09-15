package net.kotlinx.aws.lambdaCommon.handler.s3

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.kotlinx.core.koson.KosonCompanion
import net.kotlinx.core.koson.KosonObj
import net.kotlinx.core.koson.KosonSet

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
) : KosonObj {

    override fun toJson(): String = KosonSet.KSON_OTHER.encodeToString(this)

    companion object : KosonCompanion {

        override fun parseJson(json: String): S3LogicInput = KosonSet.KSON_OTHER.decodeFromString<S3LogicInput>(json)

    }

}