package net.kotlinx.core.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kotlinx.core.number.toLocalDateTime
import net.kotlinx.core.time.TimeUtil
import net.kotlinx.core.time.toLong
import java.time.LocalDateTime


object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    val zone = TimeUtil.SEOUL
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeLong(value.toLong(zone))
    override fun deserialize(decoder: Decoder): LocalDateTime = decoder.decodeLong().toLocalDateTime(zone)
}

