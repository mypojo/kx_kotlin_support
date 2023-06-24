package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.HorizontalAlignment

/**
 * 단순히 스타일이 추가된 셀
 * 래퍼 전략은 실패.. 기존 alignment 를 가져오지 못한다
 * */
@Deprecated("이거 안되네")
class XlsStyleWrapper(val del: XlsCellApply, val styleSet: XlsStyleSet) : XlsCellApply {

    override val value: String = del.value

    override fun cellApply(sheet: ExcelSheet, cell: Cell) {
        del.cellApply(sheet, cell)
        sheet.excel.lazyCallback.add {
            val exist = cell.cellStyle!!

            println("${cell.cellType}  ${exist.alignment} / ${exist.alignment.code}")

            cell.cellStyle = when (exist.alignment) {
                HorizontalAlignment.CENTER -> styleSet.center
                HorizontalAlignment.LEFT -> styleSet.left
                HorizontalAlignment.RIGHT -> styleSet.right
                else -> throw IllegalArgumentException("${exist.alignment} is not required")
            }
        }
    }

    companion object {
        fun of(data: Any, style: XlsStyleSet): XlsStyleWrapper {
            return when (data) {
                is XlsCellApply -> XlsStyleWrapper(data, style)
                else -> XlsStyleWrapper(XlsStyle(data.toString(), style.center), style)
            }
        }
    }
}