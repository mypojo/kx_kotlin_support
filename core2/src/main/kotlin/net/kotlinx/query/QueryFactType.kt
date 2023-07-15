package net.kotlinx.query

sealed interface QueryFactType {

    fun format(name: String): String

    object SUM : QueryFactType { override fun format(name: String): String = "SUM(${name}) as $name" }
    object MAX : QueryFactType { override fun format(name: String): String = "MAX(${name}) as $name" }

}