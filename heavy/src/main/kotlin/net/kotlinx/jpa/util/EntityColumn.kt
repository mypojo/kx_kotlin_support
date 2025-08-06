package net.kotlinx.jpa.util

import kotlin.reflect.KClass

/** 컬럼 */
data class EntityColumn(
    val name: String,
    val columnType: KClass<*>,
    val columnTypeGroup: KColumnTypeGroup = KColumnTypeGroup.from(columnType)
)