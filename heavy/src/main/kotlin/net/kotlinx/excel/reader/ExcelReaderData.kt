package net.kotlinx.excel.reader

/**
 * 엑셀 시트 데이터를 담는 간단한 데이터 클래스
 */
data class ExcelReaderData(
    val sheetName: String,
    val line: List<String>
)