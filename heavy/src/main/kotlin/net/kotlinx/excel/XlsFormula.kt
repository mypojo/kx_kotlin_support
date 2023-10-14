package net.kotlinx.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle

/**
 * 수식 입력기
 * ex) ROUND(SUM(${startCol}${row}:${endCol}${row}) ,0)
 * 아래처럼 오류 회피 가능
 * IF(ISERROR($furmula),$defaultValue,$furmula)
 * */
class XlsFormula(override val value: String,block: XlsFormula.() -> Unit = {}) : XlsCellApply {

    var style: CellStyle? = null

    init {
        block(this)
    }

    override fun cellApply(sheet: ExcelSheet, cell: Cell) {
        cell.cellFormula = value
        style?.let {
            sheet.excel.lazyCallback.add { cell.cellStyle = it }
        }

    }
}