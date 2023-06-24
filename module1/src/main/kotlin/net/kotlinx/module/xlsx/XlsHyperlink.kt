package net.kotlinx.module.xlsx

import net.kotlinx.core.number.StringIntUtil
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFHyperlink
import org.apache.poi.xssf.usermodel.XSSFRichTextString

class XlsHyperlink(override val value: String, block: XlsHyperlink.() -> Unit = {}) : XlsCellApply {

    /** URL 링크 */
    var urlLink: String? = null

    /** 링크 시트 명 */
    var sheetName: String? = null

    /** 보통 첫열 */
    var sheetColumn: Int = 0

    /** 보통 헤더 한칸 이후 */
    var sheetRownum: Int = 1

    init {
        block(this)
    }

    override fun cellApply(sheet: ExcelSheet, cell: Cell) {
        val creationHelper = sheet.excel.wb.creationHelper
        when {
            /** URL링크 달기  */
            urlLink != null -> {
                val link: XSSFHyperlink = creationHelper.createHyperlink(HyperlinkType.URL)
                link.address = urlLink
                cell.hyperlink = link

            }
            /**
             * 시트로 링크 달기
             * column link는 A ,B 이런식으로 네이밍된다.
             * 게다가 시트번호로는 또 안되네.
             *  */
            else -> {
                val link: XSSFHyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT)
                link.address = "$sheetName!${StringIntUtil.intToUpperAlpha(sheetColumn + 1)}${sheetRownum + 1}"
                cell.hyperlink = link
            }
        }
        cell.setCellValue(XSSFRichTextString(value))
        sheet.excel.lazyCallback.add { cell.cellStyle = sheet.excel.style.buleRight }

    }
}
