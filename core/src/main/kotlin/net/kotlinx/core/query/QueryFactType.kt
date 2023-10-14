package net.kotlinx.core.query

sealed interface QueryFactType {

    fun format(name: String): String

    data object SUM : QueryFactType { override fun format(name: String): String = "SUM(${name}) as $name" }
    data object MAX : QueryFactType { override fun format(name: String): String = "MAX(${name}) as $name" }

}