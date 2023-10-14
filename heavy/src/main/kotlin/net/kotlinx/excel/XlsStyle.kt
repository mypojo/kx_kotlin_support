package net.kotlinx.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle

/**
 * 단순히 스타일이 추가된 셀
 * */
class XlsStyle(override val value: String, val style: CellStyle) : XlsCellApply {

    override fun cellApply(sheet: ExcelSheet, cell: Cell) {
        sheet.updateCell(cell, value)
        sheet.excel.lazyCallback.add { cell.cellStyle = style }
    }
}