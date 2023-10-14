package net.kotlinx.excel

import com.google.common.collect.Lists
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import software.amazon.awssdk.annotations.NotThreadSafe

/**
 * 스래드 안전하지 않음
 */
@NotThreadSafe
class ExcelSheet(val excel: Excel, val sheet: XSSFSheet) {

    /** 고정영역 조정 : 취향이 아니라면 설정하지 말자. */
    var headerFreezePane = true

    /** 자동설정 등으로 늘어난 컬럼 너비 줄이기. 이 너비가 무슨 기준인지는 모른다. 2만이면 약 한뼘 정도 되는듯 */
    var columnWidthMax = 15000

    /** 헤더의 로우 수. 보통 헤더는 많아야 2줄이다. */
    var headerRowCnt = 0

    /**
     * 커스텀 로우단위 스타일 설정
     * 주의!! 0부터 시작하는 로우 인덱스임. 1부터 시작하는 엑셀의 인덱스 아님!
     * */
    val customRowStyleSet: MutableMap<Int, XlsStyleSet> = mutableMapOf()

    fun addHeader(titles: List<Any>): ExcelSheet {
        val row = createRow()
        for (i in titles.indices) {
            val cell = row.createCell(i)
            updateCell(cell, titles[i])
        }
        headerRowCnt++
        return this
    }

    /** 현재 커서 기준으로 로우를 추가한다. */
    fun createRow(): XSSFRow = sheet.createRow(sheet.physicalNumberOfRows)

    /** 셀 업데이트는 무조건 이거로 */
    fun updateCell(cell: Cell, value: Any?) {
        if (value == null) return
        when (value) {
            is Number -> cell.setCellValue(value.toDouble())
            is XlsCellApply -> value.cellApply(this, cell)
            else -> cell.setCellValue(XSSFRichTextString(value.toString()))
        }
    }

    fun writeLine(values: List<out Any?>) {
        writeLine(values.toTypedArray())
    }

    /** 한줄 쓰기 */
    fun writeLine(values: Array<out Any?>) {
        val row = createRow()
        for (columnIndex in values.indices) {
            val value = values[columnIndex]
            val cell = row.createCell(columnIndex)!!
            updateCell(cell, value)
        }
    }

    /**
     * 최종 쓰기 후
     * 모든 셀에 고정 스타일을 입힌다.
     * 컬럼너비를 정렬한다
     * 모든 셀에 커스텀 스타일을 적용한다
     *  */
    fun wrap(autoSizeColumn: Boolean = true, useMergedCells: Boolean = false) {
        val styles: ExcellStyle = excel.style
        val rows: Iterator<Row> = sheet.rowIterator()
        while (rows.hasNext()) {
            val thisRow = rows.next()
            val cells = thisRow.cellIterator()
            while (cells.hasNext()) {
                val thisCell = cells.next()

                val styleSet = customRowStyleSet[thisCell.rowIndex] ?: styles.normal
                val thisCellType = thisCell.cellType
                if (thisRow.rowNum < headerRowCnt) {
                    thisCell.cellStyle = styles.header
                } else {
                    thisCell.cellStyle = when (thisCellType) {
                        CellType.NUMERIC -> styleSet.right
                        CellType.FORMULA -> styleSet.right
                        else -> styleSet.left
                    }
                }
            }
        }
        if (autoSizeColumn) {
            val size: Int = sheet.getRow(0).lastCellNum.toInt()
            //자동 사이즈 조정 : ㄴ이놈은 데이터가 다 들어간 뒤 적용해줘야 하는듯 하다.
            for (i in 0 until size) {
                sheet.autoSizeColumn(i, useMergedCells)
                if (sheet.getColumnWidth(i) >= columnWidthMax) {
                    sheet.setColumnWidth(i, columnWidthMax)
                }
            }
        }
        //고정영역 조정 : 취향이 아니라면 설정하지 말자.
        if (headerFreezePane) sheet.createFreezePane(0, headerRowCnt)
    }


    //==================================================== 이하 읽기 ======================================================
    /**
     * 인메모리로 전부 읽는다.
     */
    fun readAll(): List<Row> {
        val it: Iterator<Row> = sheet.rowIterator()
        return Lists.newArrayList(it)
    }
}
