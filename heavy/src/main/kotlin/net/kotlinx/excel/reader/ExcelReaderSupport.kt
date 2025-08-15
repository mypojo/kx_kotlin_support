package net.kotlinx.excel.reader

import kotlinx.coroutines.flow.Flow
import net.kotlinx.io.input.InputResource


/**
 * 확장 함수로 사용하기 쉽게 제공
 */
fun InputResource.toFlowExcel(): Flow<ExcelReaderData> = ExcelReader.run { this@toFlowExcel.inputStream.toExcelFlow() }