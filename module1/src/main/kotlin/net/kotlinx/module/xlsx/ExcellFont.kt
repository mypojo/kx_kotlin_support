package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFFont


/**
 * 미리 정의된 스타일 세트
 */
class ExcellFont(excel: Excel) {

    val wb = excel.wb

    var configFontName = "맑은 고딕"

    // ====================== 미리 만들어놓은 스타일 / 폰트들 ==================
    /** 폰드 - 디폴트  */
    val normal: XSSFFont = wb.createFont().apply {
        fontHeightInPoints = 11.toShort()
        fontName = configFontName
    }

    /** 폰트 - 링크  */
    val blue: XSSFFont = wb.createFont().apply {
        fontHeightInPoints = 11.toShort()
        fontName = configFontName
        italic = true
        color = IndexedColors.BLUE.index
    }


    /** 폰트 - 경고  */
    val red: XSSFFont = wb.createFont().apply {
        fontHeightInPoints = 11.toShort()
        fontName = configFontName
        italic = true
        color = IndexedColors.RED.index
    }

    /** 폰트 - 수정금지  */
    val grey: XSSFFont = wb.createFont().apply {
        fontHeightInPoints = 11.toShort()
        fontName = configFontName
        strikeout = true
        color = IndexedColors.GREY_80_PERCENT.index
    }


}
