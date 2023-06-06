package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFFont

/**
 * 미리 정의된 스타일 세트
 */
class ExcellStyle(excel: Excel, font: ExcellFont) {

    val wb = excel.wb

    /** Header에 사용되는 스타일  */
    val styleHeader: CellStyle

    /** 수정 하지 말라는 뜻의 회색 블록  */
    val styleBodygrey: CellStyle

    /** thin 테두리를 가지는 일반적인 블록  */
    val styleBody: CellStyle
    val styleBodyLeft: CellStyle
    val styleBodyRight: CellStyle
    val styleBodyRed: CellStyle
    val styleBodyRedRight: CellStyle
    val styleBodyBuleRight: CellStyle
    val stylePercentage: CellStyle
    val styleLink: CellStyle

    init {

        //셀 스타일
        styleHeader = wb.createCellStyle()
        styleHeader.setFillForegroundColor(IndexedColors.YELLOW.index)
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        styleHeader.setVerticalAlignment(VerticalAlignment.forInt(1.toShort().toInt())) //중앙정렬..
        styleHeader.setAlignment(HorizontalAlignment.forInt(2.toShort().toInt())) //중앙정렬..
        boxing(styleHeader)
        styleHeader.setFont(font.normal)
        styleHeader.setWrapText(true)

        styleBody = wb.createCellStyle()
        boxing(styleBody)
        styleBody.setFont(font.normal)
        styleBodyLeft = wb.createCellStyle()
        boxing(styleBodyLeft)
        styleBodyLeft.setFont(font.normal)
        styleBodyLeft.setAlignment(HorizontalAlignment.forInt(1))
        styleBodyLeft.setVerticalAlignment(VerticalAlignment.forInt(1))
        styleBodyRight = wb.createCellStyle()
        boxing(styleBodyRight)
        styleBodyRight.setFont(font.normal)
        styleBodyRight.setAlignment(HorizontalAlignment.forInt(3.toShort().toInt()))
        styleBodyRight.setVerticalAlignment(VerticalAlignment.forInt(1.toShort().toInt()))
        //\BODY_Number.setDataFormat(XSSFDataFormat.getBuiltinFormat("#,##0.00")); // 나중에 사용하자.
        styleBodyRed = buildStyle(font.red, null)
        styleBodyRed.alignment = HorizontalAlignment.forInt(2.toShort().toInt())
        styleBodyRed.verticalAlignment = VerticalAlignment.forInt(1.toShort().toInt())
        styleBodyRedRight = wb.createCellStyle()
        styleBodyRedRight.setFont(font.red)
        styleBodyRedRight.setAlignment(HorizontalAlignment.forInt(3.toShort().toInt()))
        styleBodyRedRight.setVerticalAlignment(VerticalAlignment.forInt(1.toShort().toInt()))
        styleBodyBuleRight = wb.createCellStyle()
        styleBodyBuleRight.setFont(font.blue)
        styleBodyBuleRight.setAlignment(HorizontalAlignment.forInt(3.toShort().toInt()))
        styleBodyBuleRight.setVerticalAlignment(VerticalAlignment.forInt(1.toShort().toInt()))
        stylePercentage = wb.createCellStyle()
        stylePercentage.setDataFormat(wb.createDataFormat().getFormat("0.00%"))
        stylePercentage.setAlignment(HorizontalAlignment.forInt(3.toShort().toInt()))
        stylePercentage.setVerticalAlignment(VerticalAlignment.forInt(1.toShort().toInt()))
        styleBodygrey = wb.createCellStyle()
        styleBodygrey.setFillForegroundColor(22.toShort()) //GREY_25_PERCENT
        styleBodygrey.setFillPattern(FillPatternType.SOLID_FOREGROUND) //SOLID_FOREGROUND
        boxing(styleBodygrey)
        styleBodygrey.setFont(font.normal)
        styleLink = wb.createCellStyle()
        boxing(styleLink)
        styleLink.setFont(font.blue)
        styleLink.setVerticalAlignment(VerticalAlignment.forInt(1.toShort().toInt()))
    }

    /** 간단 스타일 빌드. addStyle과 한께 쓰지.
     * ex) XSSFColor.GREY_25_PERCENT.index
     */
    fun buildStyle(font: XSSFFont? = null, foregroundColor: Short? = null): CellStyle {
        val style: CellStyle = wb.createCellStyle()
        boxing(style)
        if (font != null) style.setFont(font)
        if (foregroundColor != null) {
            style.fillForegroundColor = foregroundColor
            style.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        //style.setVerticalAlignment((short)1);  //중앙정렬..
        //style.setAlignment((short)2);  //중앙정렬..
        return style
    }

    companion object {
        /**
         * 스타일에 박스테두리 삽입
         */
        private fun boxing(style: CellStyle) {
            //cellStyle.setWrapText( true ); //- 박스안에 다넣기
            style.borderBottom = BorderStyle.THIN
            style.bottomBorderColor = IndexedColors.BLACK.index
            style.borderLeft = BorderStyle.THIN
            style.leftBorderColor = IndexedColors.BLACK.index
            style.borderRight = BorderStyle.THIN
            style.rightBorderColor = IndexedColors.BLACK.index
            style.borderTop = BorderStyle.THIN
            style.topBorderColor = IndexedColors.BLACK.index
        }
    }
}
