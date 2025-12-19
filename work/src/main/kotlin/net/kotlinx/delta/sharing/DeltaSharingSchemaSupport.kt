package net.kotlinx.delta.sharing

import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import org.apache.avro.generic.GenericFixed
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Schema.unwrapNullable(): Schema {
    return if (this.type == Schema.Type.UNION) {
        this.types.first { it.type != Schema.Type.NULL }
    } else {
        this
    }
}

fun Schema.decodeValue(rawValue: Any?): Any? {
    if (rawValue == null) return null

    val actualSchema = this.unwrapNullable()
    val logicalType = actualSchema.logicalType

    return when (logicalType?.name) {

        // ---------- DATE ----------
        LogicalTypes.date().name -> {
            val days = rawValue as Int
            LocalDate.ofEpochDay(days.toLong())
                .format(DateTimeFormatter.ISO_DATE) // yyyy-MM-dd
        }

        // ---------- DECIMAL (FIXED) ----------
        LogicalTypes.decimal(0, 0).name -> {
            val decimal = logicalType as LogicalTypes.Decimal

            val bytes = when (rawValue) {
                is GenericFixed -> rawValue.bytes()
                is ByteArray -> rawValue
                else -> error("Unsupported decimal value type: ${rawValue::class}")
            }

            val unscaled = BigInteger(bytes) // signed, big-endian
            BigDecimal(unscaled, decimal.scale).toDouble()
        }

        // ---------- DEFAULT ----------
        else -> rawValue
    }
}
