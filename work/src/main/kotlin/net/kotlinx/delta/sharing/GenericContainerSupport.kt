package net.kotlinx.delta.sharing

import net.kotlinx.string.toTextGridPrint
import org.apache.avro.generic.GenericRecord

/**
 * CSV 용으로 간단 변경
 * 로우타입을 적절히 컨버팅 해줘야함
 * */
fun GenericRecord.toList(): List<String> = this.schema.fields.map { field ->
    val rawValue = this[field.name()]
    field.schema().decodeValue(rawValue).toString()
}


fun GenericRecord.printSimple() {
    listOf("컬럼", "타입", "값").toTextGridPrint {
        this.schema.fields.map { field ->
            val rawValue = this[field.name()]
            val schema = field.schema().unwrapNullable()
            val value = field.schema().decodeValue(rawValue) ?: "null"
            arrayOf(field.name(), schema, value)
        }
    }
}
