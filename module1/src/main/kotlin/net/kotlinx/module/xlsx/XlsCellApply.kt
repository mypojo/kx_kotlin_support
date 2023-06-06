package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.Cell

interface XlsCellApply {

    val value: String

    fun cellApply(excelSheet: ExcelSheet, cell: Cell)
}