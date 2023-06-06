package net.kotlinx.module.xlsx

import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*

/**
 * @desc 엑셀2002 이상 버전의 poi 어뎁터
 * 네이밍이 너무 구려서 확장으로 하지 않고 래퍼로 만듬
 */
class Excel(
    val wb: XSSFWorkbook = XSSFWorkbook()
) {

    /** 기본 폰트 */
    val font = ExcellFont(this)

    /** 기본 스타일 */
    val style: ExcellStyle = ExcellStyle(this, font)

    /**
     * 스타일은 래핑할때 일괄 디폴트값으로 셑팅하기때문에
     * ex1) 셀별로 개별 스타일 적용하고 싶으면 여기 등록
     * ex2)  일괄 wrap 후 부분적으로 컬럼너비를 조절하기 위해서 사용된다. 보통 600*10 정도 주면 되는듯?  sheet.setColumnWidth(columnIndex, width)
     * */
    val lazyCallback: MutableList<() -> Unit> = mutableListOf()


    /** 생성된 시트들. 래핑등에 필요한 부가정보들을 함게 들고있다.  */
    val sheets: MutableMap<String, ExcelSheet> = LinkedHashMap()

    /** 신규 시트 생성  */
    fun createSheet(sheetname: String): ExcelSheet {
        val sheetAt: XSSFSheet = wb.createSheet(sheetname)
        val sheet = ExcelSheet(this, sheetAt)
        sheets[sheetname] = sheet
        return sheet
    }

    /**
     * 각 행을 실선을 둘러싼다.
     * 가장 긴 열에 맞추어 정렬한다.
     */
    fun wrap(): Excel {
        for (sheet in sheets.values) sheet.wrap()
        //늦은 스타일들을 적용해준다.
        lazyCallback.forEach { it() }
        return this
    }

    //==================================================== delegate ======================================================

    /** delegate : 처음 파일을 열때 보이는 시트 지정  */
    fun setActiveSheet(sheetIndex: Int): Excel {
        wb.setActiveSheet(sheetIndex)
        return this
    }


    //==================================== OUT =============================================
    fun write(file: File) {
        FileOutputStream(file).use {
            write(it)
        }
    }

    /** 비밀번호 걸어서 저장 */
    fun write(file: File, pwd: String) {
        val temp = File(file.parentFile, file.name + ".tmp")
        write(temp)
        ExcelUtil.encrypt(temp, pwd, file)
        temp.delete()
    }

    /**
     * ex0
     * response.setContentType("application/vnd.ms-excel"); //charset=utf-8
     * ServletOutputStream out = response.getOutputStream();
     * write(out);
     *  확인해야함!!
     */
    fun write(out: OutputStream) {
        out.use {
            wb.write(out)
            //out.flush()
        }
    }

    /** 전체 데이터를 간단히 읽는다 */
    fun readAll(): LinkedHashMap<String, List<List<String>>> {
        val result = LinkedHashMap<String, List<List<String>>>()
        wb.sheetIterator().forEach { sheet ->
            val lines = mutableListOf<List<String>>()
            sheet.rowIterator().forEach { row ->
                lines += ExcelReadUtil.toListString(row)
            }
            result[sheet.sheetName] = lines
        }
        return result
    }


    companion object {

        fun from(file: File): Excel = from(FileInputStream(file))

        /** ex) of(clazz.getResourceAsStream(path))  */
        fun from(inputStream: InputStream): Excel = inputStream.use {
            val wb = XSSFWorkbook(inputStream)
            Excel(wb)//일단 이후에 아무것도 안함
        }

    }
}
