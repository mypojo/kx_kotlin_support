package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFRichTextString

/**
 * 박스 크기는 그냥 디폴트로 두면 될듯
 * */
class XlsComment(override val value: String,block: XlsComment.() -> Unit = {}) : XlsCellApply {

    lateinit var comments: List<String>

    var col01 = 1

    /** 코멘트 디폴트 가로길이  */
    var col02 = 5

    var row01 = 1

    /** 코멘트 디폴트 세로길이  */
    var row02 = 5

    var author: String? = null

    init {
        block(this)
    }

    /**
     * 특정 컬럼에 코멘트 추가
     * ex) totalSheet.addComments(XlsComment.builder().commentLines(e.getValue()).row(0).col(i+1).build());
     */
    override fun cellApply(excelSheet: ExcelSheet, cell: Cell) {
        val helper = excelSheet.excel.wb.creationHelper
        val anchor = helper.createClientAnchor().apply {
            setCol1(col01)
            setCol2(col02)
            this.row1 = row01
            this.row2 = row02
        }
        cell.cellComment = excelSheet.sheet.createDrawingPatriarch().createCellComment(anchor).apply {
            string = helper.createRichTextString(comments.joinToString("\n"))
            author = author
        }
        cell.setCellValue(XSSFRichTextString(value))
    }

}
