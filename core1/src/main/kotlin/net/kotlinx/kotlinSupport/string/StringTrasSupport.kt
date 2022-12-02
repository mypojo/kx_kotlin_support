package net.kotlinx.kotlinSupport.string

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

fun String.toSnakeFromCamel(): String {
    return camelRegex.replace(this) { "_${it.value}" }.toLowerCase()
}

fun String.toCamelFromSnake(): String {
    return snakeRegex.replace(this) { it.value.replace("_", "").toUpperCase() }
}
