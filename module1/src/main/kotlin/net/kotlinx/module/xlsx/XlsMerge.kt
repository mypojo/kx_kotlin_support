//package net.kotlinx.module.xlsx
//
//import com.epe.util.root.Incompleted
//import com.google.common.base.Strings
//import org.apache.poi.ss.usermodel.Cell
//import org.apache.poi.ss.usermodel.CellType
//import org.apache.poi.ss.usermodel.Row
//import org.apache.poi.ss.util.CellRangeAddress
//
//
//class XlsMerge(
//    private val sheet: ExcelSheet
//) {
//
//    private val lastValues = arrayOfNulls<String>(1000)
//    private val startRows = arrayOfNulls<Int>(1000)
//    private val startKeyRows = arrayOfNulls<Int>(1000)
//    private var row: Row? = null
//    private var startCol: Int? = null
//    private var lastValue = ""
//    private var lastKeyValue: String? = ""
//    private var ableRow: Array<Int>?
//    private var ableCol: Array<Int>?
//    private var keyCol: Int? = null
//    private var ableKeyCol: Array<Int>?
//
//    //    private Map<Integer,Integer[]> with = new HashMap<Integer, Integer[]>(); //???????????
//    //    public XlsMerge setWithMerge(Integer tergerIndex,Integer ... colIndexs){
//    //    	with.put(tergerIndex, colIndexs);
//    //    	return this;
//    //    }
//
//    fun merge() {
//        val rows: Iterator<Row> = sheet.rowIterator()
//        while (rows.hasNext()) {
//            row = rows.next()
//            val rowIndex = row!!.rowNum
//            var currentKeyValue: String? = null
//            if (keyCol != null) currentKeyValue = getVaueToString(row!!.getCell(keyCol!!))
//            val cells = row!!.cellIterator()
//            while (cells.hasNext()) {
//                val thisCell = cells.next()
//                val colIndex = thisCell.columnIndex
//                mergeRowByKey(rows, rowIndex, colIndex, currentKeyValue)
//                val value = getVaueToString(thisCell)
//                mergeCol(rowIndex, colIndex, value)
//                mergeRow(rows, rowIndex, colIndex, value)
//            }
//            lastKeyValue = currentKeyValue
//        }
//        setAlignment()
//    }
//
//    /** 머지를 비교하기 위해 숫자도 문자로 취급한다.  */
//    private fun getVaueToString(thisCell: Cell): String {
//        var value = ""
//        if (thisCell.cellType == CellType.STRING) value = thisCell.richStringCellValue.string else if (thisCell.cellType == CellType.NUMERIC) value =
//            thisCell.numericCellValue.toString()
//        return value
//    }
//
//    /** 컬럼머지가 가능하면  헤더로 판단하고 스킵한다.  */
//    private fun mergeRowByKey(rows: Iterator<Row>, rowIndex: Int, colIndex: Int, currentKeyValue: String?) {
//        if (currentKeyValue == null) return
//        if (isColMergeAble(rowIndex)) return  //
//        if (!isRowKeyMergeAble(rowIndex, colIndex)) return
//        if (currentKeyValue == lastKeyValue) {
//            if (startKeyRows[colIndex] == null) startKeyRows[colIndex] = rowIndex - 1 //최초 설정.
//            if (!rows.hasNext()) {  //마지막일경우 발동
//                sheet.addMergedRegion(CellRangeAddress(startKeyRows[colIndex], rowIndex, colIndex, colIndex))
//                startKeyRows[colIndex] = null
//            }
//        } else if (startKeyRows[colIndex] != null) {
//            sheet.addMergedRegion(CellRangeAddress(startKeyRows[colIndex], rowIndex - 1, colIndex, colIndex))
//            startKeyRows[colIndex] = null
//        }
//    }
//
//    /**
//     * 이전 값과 비교하여 merge를 결정한다.  가로 머지이다.. 이름 같이 지었네.. ㅈㅅ. -
//     */
//    private fun mergeCol(rowIndex: Int, colIndex: Int, value: String) {
//        if (!isColMergeAble(rowIndex)) return
//        if (lastValue == value) {
//            if (startCol == null) startCol = colIndex - 1
//            if (colIndex == row!!.lastCellNum - 1) {  //마지막일경우 발동
//                //sheet.addMergedRegion(new Region(rowIndex,startCol.shortValue(),rowIndex,(short)(colIndex)));
//                sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex, startCol, colIndex))
//                startCol = null
//            }
//        } else if (startCol != null) {
//            //sheet.addMergedRegion(new Region(rowIndex,startCol.shortValue(),rowIndex,(short)(colIndex-1)));
//            sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex, startCol, colIndex - 1))
//            startCol = null
//        }
//        lastValue = value
//    }
//
//    /**
//     * 가로 머지가 가능한지?
//     * null이면 모두 가능하다고 판단한다.
//     */
//    private fun isColMergeAble(rowIndex: Int): Boolean {
//        if (ableRow == null) return true
//        for (thisAbleRow in ableRow!!) if (thisAbleRow == rowIndex) return true
//        return false
//    }
//
//    /**
//     * 세로 머지가 가능한지?
//     * null이면 모두 불가능하다고 판단한다.
//     * 가로 머지가 가능한 열은 헤더로 판한하고 세로 머지도 가능하다고 본다.  (헤더 정보도 가지고 있지만 확장을 위해..)
//     * 가로세로 중복의 경우 비교행을 지나서 판별하는 로직이 있음으로 -1을 한것을 같이 비교해 준다.
//     */
//    private fun isRowMergeAble(rowIndex: Int, colIndex: Int): Boolean {
//        if (isColMergeAble(rowIndex) || isColMergeAble(rowIndex - 1)) return true
//        if (ableCol == null) return false
//        for (thisAbleCol in ableCol!!) if (thisAbleCol == colIndex) return true
//        return false
//    }
//
//    private fun isRowKeyMergeAble(rowIndex: Int, colIndex: Int): Boolean {
//        if (ableKeyCol == null) return false
//        for (thisAbleCol in ableKeyCol!!) if (thisAbleCol == colIndex) return true
//        return false
//    }
//
//    /**
//     * 이전 값과 비교하여 merge를 결정한다.
//     * 세로 방향의 값을 머지한다.
//     */
//    private fun mergeRow(rows: Iterator<Row>, rowIndex: Int, colIndex: Int, value: String) {
//        if (!isRowMergeAble(rowIndex, colIndex)) return
//        if (Strings.isNullOrEmpty(value)) return  //별생각없이 하드코딩함. 빈값이면 머지하지 않는다.
//        if (value == lastValues[colIndex]) {
//            if (startRows[colIndex] == null) startRows[colIndex] = rowIndex - 1 //최초 설정.
//            if (!rows.hasNext()) {  //마지막일경우 발동
//                sheet.addMergedRegion(CellRangeAddress(startRows[colIndex], rowIndex, colIndex, colIndex))
//                //            	mergeWith(rowIndex, colIndex);
//                startRows[colIndex] = null
//            }
//        } else if (startRows[colIndex] != null) {
//            //sheet.addMergedRegion(new Region(startRows[colIndex],(short)(colIndex),rowIndex-1,(short)(colIndex)));
//            sheet.addMergedRegion(CellRangeAddress(startRows[colIndex], rowIndex - 1, colIndex, colIndex))
//            //        	mergeWith(rowIndex-1, colIndex);
//            startRows[colIndex] = null
//        }
//        lastValues[colIndex] = value
//    }
//    //	private void mergeWith(int rowIndex, int colIndex) {
//    //		Integer[] withs = with.get(colIndex);
//    //		if(withs==null) return;
//    //		for (Integer integer : withs) {
//    //			System.err.println(startRows[colIndex]);
//    //			System.err.println(rowIndex);
//    //			sheet.addMergedRegion(new CellRangeAddress(startRows[colIndex],rowIndex,integer,integer));
//    //		}
//    //	}
//    /**
//     * 머지 가능한 열을 입력한다.헤더만 할 경우 헤더 로우를 입력한다.
//     */
//    fun setAbleRow(vararg ableRow: Int): XlsMerge {
//        this.ableRow = ableRow
//        return this
//    }
//
//    /**
//     * Key기준으로 머지할 로우를 선택한다.
//     * 각 행의 key끼리 같으면 해당 행의 값과 상관없이 머지한다.
//     */
//    fun setAbleKeyCol(keyCol: Int?, vararg ableKeyCol: Int): XlsMerge {
//        this.keyCol = keyCol
//        this.ableKeyCol = ableKeyCol
//        return this
//    }
//
//    /**
//     * 머지할 컬럼을 입력한다. null이면 모두 불가능하다고 판단한다.
//     */
//    fun setAbleCol(vararg ableCol: Int): XlsMerge {
//        this.ableCol = ableCol
//        return this
//    }
//    /**
//     * 해당 시트의 가로/세로를 머지한다.
//     * Wrap 이전?에 호출되어야 한다.
//     * ex) p.getMerge(1).setAbleRow(0).setAbleCol(0).merge();
//     */
//    //    public XlsMerge getMerge(int index){
//    //        return new Merge(wb.getSheetAt(index));
//    //    }
//}
