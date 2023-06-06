package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFRichTextString

class XlsComment(override val value: String) : XlsCellApply {

    lateinit var comments: List<String>

    var col01 = 1

    /** 코멘트 디폴트 가로길이  */
    var col02 = 5

    var row01 = 1

    /** 코멘트 디폴트 세로길이  */
    var row02 = 5

    var author: String? = null

    /**
     * 특정 컬럼에 코멘트 추가
     * ex) totalSheet.addComments(XlsComment.builder().commentLines(e.getValue()).row(0).col(i+1).build());
     */
    override fun cellApply(excelSheet: ExcelSheet, cell: Cell) {
        val helper = excelSheet.excel.wb.creationHelper
        val anchor = helper.createClientAnchor().apply {
            setCol1(cell.columnIndex + col1)
            setCol2(cell.columnIndex + col2)
            row1 = cell.rowIndex + row01
            row2 = cell.rowIndex + row02
        }
        val comment = excelSheet.sheet.createDrawingPatriarch().createCellComment(anchor).apply {
            string = helper.createRichTextString(comments.joinToString("\n"))
            author = author
        }
        cell.cellComment = comment
        cell.setCellValue(XSSFRichTextString(value))
    }

}
