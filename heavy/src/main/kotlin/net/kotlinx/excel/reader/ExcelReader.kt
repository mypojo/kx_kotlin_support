package net.kotlinx.excel.reader

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import mu.KotlinLogging
import net.kotlinx.core.VibeCoding
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.model.SharedStringsTable
import org.apache.poi.xssf.usermodel.XSSFComment
import org.xml.sax.InputSource
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

/**
 * 대용량 엑셀 파일을 SAX로 읽어서 Flow로 반환하는 지원 클래스
 */
@VibeCoding
object ExcelReader {

    private val log = KotlinLogging.logger {}

    /**
     * InputStream으로부터 엑셀 파일을 읽어서 시트별로 Flow를 생성
     * 현재는 간단한 방식을 사용 (SAX 방식은 문제가 있어서 주석 처리)
     */
    fun InputStream.toExcelFlow(): Flow<ExcelReaderData> = toExcelFlowSax()

    /**
     * SAX 방식으로 엑셀 파일 읽기 (현재 문제가 있어서 사용 안 함)
     */
    fun InputStream.toExcelFlowSax(): Flow<ExcelReaderData> = channelFlow {
        use { inputStream ->
            val opcPackage = OPCPackage.open(inputStream)
            val xssfReader = XSSFReader(opcPackage)
            val sharedStringsTable = xssfReader.sharedStringsTable as SharedStringsTable
            val stylesTable = xssfReader.stylesTable

            // 시트 이름들을 가져옴
            val workbook = xssfReader.workbookData
            val sheetNames = getSheetNames(workbook)

            // 각 시트를 순회하면서 데이터 읽기
            val sheetsIterator = xssfReader.sheetsData
            var sheetIndex = 0

            while (sheetsIterator.hasNext()) {
                val sheetInputStream = sheetsIterator.next()

                val sheetName = sheetNames.getOrElse(sheetIndex) { "Sheet${sheetIndex + 1}" }
                val dataChannel = Channel<ExcelReaderData>(Channel.UNLIMITED)
                val contentHandler = ExcelContentHandler(sheetName, dataChannel)

                log.debug { "Processing sheet '$sheetName' (index: $sheetIndex)" }

                try {
                    sheetInputStream.use { inputStream ->
                        log.debug { "Starting to process sheet with SharedStrings: ${sharedStringsTable != null}, Styles: ${stylesTable != null}" }

                        val saxParserFactory = SAXParserFactory.newInstance()
                        saxParserFactory.isNamespaceAware = true
                        val saxParser = saxParserFactory.newSAXParser()
                        val xmlReader = saxParser.xmlReader

                        // XSSFSheetXMLHandler 생성 시 더 안전하게 처리
                        val sheetHandler = if (sharedStringsTable != null && sharedStringsTable is SharedStringsTable) {
                            XSSFSheetXMLHandler(stylesTable, sharedStringsTable, contentHandler, false)
                        } else {
                            // SharedStringsTable이 없는 경우 빈 테이블로 처리
                            log.debug { "Creating handler without SharedStringsTable" }
                            XSSFSheetXMLHandler(stylesTable, null, contentHandler, false)
                        }

                        xmlReader.contentHandler = sheetHandler

                        val inputSource = InputSource(inputStream)
                        log.debug { "About to parse XML..." }
                        xmlReader.parse(inputSource)
                        log.debug { "XML parsing completed" }
                    }
                } finally {
                    dataChannel.close()
                }

                // 채널에서 데이터를 읽어서 emit
                dataChannel.consumeAsFlow().collect { data ->
                    send(data)
                }

                log.debug { "Sheet '$sheetName' processing completed" }

                sheetIndex++
            }

            opcPackage.close()
        }
    }


    private fun getSheetNames(workbookInputStream: InputStream): List<String> {
        return try {
            val content = workbookInputStream.bufferedReader().readText()
            val sheetNames = mutableListOf<String>()

            // <sheet> 태그에서 name 속성 추출
            val sheetRegex = """<sheet[^>]+name="([^"]+)"""".toRegex()
            sheetRegex.findAll(content).forEach { match ->
                sheetNames.add(match.groupValues[1])
            }

            log.debug { "Found sheet names: $sheetNames" }
            sheetNames
        } catch (e: Exception) {
            log.debug(e) { "Error parsing sheet names: ${e.message}" }
            emptyList()
        } finally {
            workbookInputStream.close()
        }
    }

    /**
     * SAX 이벤트를 처리하는 핸들러
     */
    private class ExcelContentHandler(private val sheetName: String, private val dataChannel: Channel<ExcelReaderData>) : XSSFSheetXMLHandler.SheetContentsHandler {

        private val currentRow = mutableListOf<String>()
        private var currentCol = 0
        private var currentRowNum = 0

        override fun startRow(rowNum: Int) {
            currentRow.clear()
            currentCol = 0
            currentRowNum = rowNum
            if (rowNum < 5) {
                log.debug { "Starting row $rowNum" }
            }
        }

        override fun endRow(rowNum: Int) {
            // 각 행을 개별적으로 채널에 전송
            if (currentRow.isNotEmpty()) {
                dataChannel.trySend(ExcelReaderData(sheetName, currentRow.toList()))
            }
            if (rowNum < 5) { // 처음 5행만 디버그 출력
                log.debug { "Row $rowNum: $currentRow" }
            }
        }

        override fun cell(cellReference: String?, formattedValue: String?, comment: XSSFComment?) {
            // 빈 셀들을 채우기 위해 컬럼 인덱스 계산
            val colIndex = getColumnIndex(cellReference)

            // 빈 셀들을 빈 문자열로 채움
            while (currentRow.size <= colIndex) {
                currentRow.add("")
            }

            // 현재 셀 값 설정
            if (colIndex < currentRow.size) {
                currentRow[colIndex] = formattedValue ?: ""
            }

            currentCol = colIndex + 1

            // 디버그: 처음 몇 개 행의 셀만 출력
            if (currentRowNum < 3 && colIndex < 5) {
                log.debug { "Cell[$cellReference] = '$formattedValue' at index $colIndex" }
            }
        }

        private fun getColumnIndex(cellReference: String?): Int {
            if (cellReference == null) return currentCol

            val colRef = cellReference.replace(Regex("\\d+"), "")
            var colIndex = 0

            for (i in colRef.indices) {
                colIndex = colIndex * 26 + (colRef[i] - 'A' + 1)
            }

            return colIndex - 1
        }
    }

}

