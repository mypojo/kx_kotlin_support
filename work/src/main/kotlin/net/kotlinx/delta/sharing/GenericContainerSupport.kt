package net.kotlinx.delta.sharing

import org.apache.avro.generic.GenericRecord

/**
 * CSV 용으로 간단 변경
 * */
fun GenericRecord.toList(): List<String> = this.schema.fields.map { field -> this[field.name()]?.toString() ?: "" }
