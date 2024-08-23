package net.kotlinx.aws.athena.table

/**
 * 아테나 타입
 * */
class AthenaType(val type: Any) {

    /** 스키마를 athena가 인식하는 문자열로 변경해준다 */
    override fun toString(): String = when (type) {

        /** 객체 매핑 */
        is Map<*, *> -> "STRUCT< ${type.entries.joinToString(",\n") { "${it.key}:${it.value}" }} >"

        /** 객체를 array로 매핑 */
        is List<*> -> {
            "ARRAY<STRUCT< ${
                type.joinToString(",") {
                    val pair = it as Pair<String, Any>? ?: throw IllegalArgumentException("pair 로 입력해야 합니다")
                    "${pair.first}:${pair.second}"
                }
            } >>"
        }

        is CharSequence -> type.toString()

        else -> throw RuntimeException("지원하지 않는 타입입니다 ${type::class}")
    }

}