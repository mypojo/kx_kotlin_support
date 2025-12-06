package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.InputColumnDataType

/**
 * Athena 컬럼 타입 문자열을 QuickSight InputColumnDataType 으로 매핑
 * QuickSight가 지원하는 타입 범위에 맞춰 보수적으로 매핑함
 */
fun String.toQuickSightType(): InputColumnDataType = when (this.lowercase()) {
    // 문자열 계열
    "varchar", "char", "string", "json" -> InputColumnDataType.String

    // 정수 계열
    "bigint", "int", "integer", "smallint", "tinyint" -> InputColumnDataType.Integer

    // 실수/소수 계열
    "double", "float", "real", "decimal", "numeric" -> InputColumnDataType.Decimal

    // 날짜/시간
    "date", "timestamp" -> InputColumnDataType.Datetime

    // 그 외(배열, 맵, 구조체, 불린 등)는 문자열로 처리
    else -> InputColumnDataType.String
}
