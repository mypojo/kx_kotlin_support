package net.kotlinx.json.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.string.toBigDecimal2
import net.kotlinx.time.TimeUtil
import net.kotlinx.time.toLong
import java.time.LocalDateTime


/**
 * kotlinx.serialization 이 전반적으로 신뢰도가 매우 떨어짐
 * 그냥 GSON 사용할것
 * */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {

    val zone = TimeUtil.SEOUL

    /** 가성비 좋은 Long 으로 변환 */
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeLong(value.toLong(zone))

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val time = try {
            decoder.decodeLong()
        } catch (e: SerializationException) {
            //println(decoder.decodeString())
            val text = decoder.decodeString()
            //1.730184960092E12 이런식으로 표현되는 경우가 있음.. 이경우 별도 파싱
            text.toBigDecimal2().toLong()
        }
        return time.toLocalDateTime(zone)
    }
}

