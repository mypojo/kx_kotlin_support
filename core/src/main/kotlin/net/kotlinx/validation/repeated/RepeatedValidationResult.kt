package net.kotlinx.validation.repeated

import net.kotlinx.string.abbr
import net.kotlinx.time.toTimeString

data class RepeatedValidationResult(
    val config: RepeatedValidation,
    val duration: Long,
    val success: Boolean,
    val msgs: List<String>,
) {
    fun toGridArray(): Array<*> {
        return arrayOf(
            config.group,
            config.code,
            config.desc.joinToString(",").abbr(60),
            config.authors.joinToString(",") { it.id },
            config.range,
            duration.toTimeString(),
            success,
            msgs.joinToString(",").abbr(60),
        )
    }
}