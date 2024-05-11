package net.kotlinx.json.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.time.TimeUtil
import net.kotlinx.time.toLong
import java.time.LocalDateTime


object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    val zone = TimeUtil.SEOUL
    /** 가성비 좋은 Long 으로 변환 */
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeLong(value.toLong(zone))
    override fun deserialize(decoder: Decoder): LocalDateTime = decoder.decodeLong().toLocalDateTime(zone)
}

