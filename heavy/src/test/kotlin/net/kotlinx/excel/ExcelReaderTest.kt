package net.kotlinx.excel

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import net.kotlinx.excel.reader.toFlowExcel
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

class ExcelReaderTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("ExcelReader") {

            Then("기존 Excel 파일 읽기 테스트") {
                // 테스트용 Excel 파일이 있다면 사용
                val excelFile = ResourceHolder.WORKSPACE.parentFile.slash("자료저장\\더미데이터\\INPUT DATA Type 정리_애드테크팀_240907.xlsx")

                log.info { "전체파일읽기" }
                excelFile.toInputResource().toFlowExcel().collect { row ->
                    log.debug { " -> Sheet: ${row.sheetName} | Line: ${row.line.joinToString("\t")}" }
                }

                log.info { "부분파일읽기" }
                excelFile.toInputResource().toFlowExcel().take(3).collect { row ->
                    log.info { " -> Sheet: ${row.sheetName} | Line: ${row.line.joinToString("\t")}" }
                }
            }

            Then("간단한 Excel 파일 생성 및 읽기 테스트") {
                // 테스트용 Excel 파일 생성
                val testFile = createTestExcelFile()

                try {
                    val excelData = testFile.toInputResource().toFlowExcel().take(10).toList()
                    excelData.forEach { rowData ->
                        println("Sheet: ${rowData.sheetName} | Line: ${rowData.line.joinToString(" | ")}")
                    }
                } finally {
                    // 테스트 파일 정리
                    if (testFile.exists()) {
                        testFile.delete()
                    }
                }
            }
        }
    }

    private fun createTestExcelFile(): java.io.File {
        val tempFile = java.io.File.createTempFile("test_excel", ".xlsx")

        // Apache POI를 사용해서 간단한 Excel 파일 생성
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()

        // 첫 번째 시트
        val sheet1 = workbook.createSheet("API연동")
        val row1 = sheet1.createRow(0)
        row1.createCell(0).setCellValue("컬럼1")
        row1.createCell(1).setCellValue("컬럼2")
        row1.createCell(2).setCellValue("컬럼3")

        val row2 = sheet1.createRow(1)
        row2.createCell(0).setCellValue("데이터1")
        row2.createCell(1).setCellValue("데이터2")
        row2.createCell(2).setCellValue("데이터3")

        // 두 번째 시트
        val sheet2 = workbook.createSheet("수동업로드")
        val row3 = sheet2.createRow(0)
        row3.createCell(0).setCellValue("제목A")
        row3.createCell(1).setCellValue("제목B")

        val row4 = sheet2.createRow(1)
        row4.createCell(0).setCellValue("값A")
        row4.createCell(1).setCellValue("값B")

        // 파일에 쓰기
        tempFile.outputStream().use { fileOut ->
            workbook.write(fileOut)
        }
        workbook.close()

        println("DEBUG: Test Excel file created at: ${tempFile.absolutePath}")
        return tempFile
    }
}