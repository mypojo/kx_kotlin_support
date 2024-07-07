package net.kotlinx.knotion

import net.kotlinx.time.isDate
import net.kotlinx.time.toLocalDateTime
import net.kotlinx.time.toYmdF01
import net.kotlinx.time.toYmdhmKr01
import org.jraf.klibnotion.model.date.DateOrDateRange
import org.jraf.klibnotion.model.date.Timestamp
import org.jraf.klibnotion.model.property.SelectOption
import org.jraf.klibnotion.model.property.value.PropertyValue
import org.jraf.klibnotion.model.richtext.RichTextList

/**
 * https://github.com/BoD/klibnotion
 * 2023 기준 오픈소스 노션 SDK 중 가장 쓸만한거
 *  */

/** 노션은 UTC로 저장한다. 그냥 그대로 쓰자. */
fun Timestamp.toText(): String {
    val time = this.toLocalDateTime(net.kotlinx.time.TimeUtil.UTC)
    return if (time.isDate()) {
        time.toLocalDate().toYmdF01()
    } else {
        time.toYmdhmKr01()
    }
}

/** 간단 텍스트로 리턴해줌 */
fun PropertyValue<*>.toValueString(): String {
    return when (val value = this.value!!) {
        is SelectOption -> "${value.name}"
        is RichTextList -> "${value.plainText}"
        is DateOrDateRange -> {
            if (value.end == null) {
                value.start.timestamp.toText()
            } else {
                "${value.start.timestamp.toText()} ~ ${value.end!!.timestamp.toText()}"
            }
        }

        else -> throw IllegalArgumentException("${value::class} is not required")
    }
}