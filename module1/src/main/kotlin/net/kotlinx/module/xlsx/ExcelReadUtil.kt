package net.kotlinx.module.xlsx

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFCell

/**
 * 실제 문자열 변환 등의 유틸
 */
object ExcelReadUtil {

    /** 간단 읽기용.  */
    fun toListString(row: Row): List<String> {
        val cellNum = row.lastCellNum.toInt()
        if (cellNum == -1) return emptyList()

        val line = mutableListOf<String>()
        val cells: Iterator<Cell> = row.iterator()
        while (cells.hasNext()) {
            val eachCell = cells.next()
            line += toString(eachCell)
        }
        return line
    }

    /**
     * CELL_TYPE_NUMERIC의 경우 double임으로 2 => 2.0 이런식으로 바뀐다.
     * BigDecimal로 변경함으로 성능 문제시 교체하자.
     * DateUtil 의 is~~ 시리즈가 완벽하게 작동하지 않는다. 따라서 안되는 부분은 DateUtil.getJavaDate(cell.getNumericCellValue() 를 사용
     * DateUtil -> XSSF에서도 이게 통하는지는 의문.. . 걍 time을 일단은 문자로 넘겨준다.
     * 나증에 Object 로 이동하도록 변경하자
     *
     * double로 읽을경우 --> 24.7 일케 더블이 들어오면 24.6999999999 일케 바껴벼린다.
     */
    fun toString(cell: Cell?): String {
        if (cell == null) return ""
        return if (cell.cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) cell.dateCellValue.time.toString() else {
                val rawCell = cell as XSSFCell
                rawCell.rawValue //number를 double로 읽기 않기 위해 원시값을 불러온다.
            }
        } else cell.richStringCellValue.string.trim { it <= ' ' }
    }
}
