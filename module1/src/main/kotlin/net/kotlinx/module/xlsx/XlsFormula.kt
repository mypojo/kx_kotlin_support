package net.kotlinx.module.xlsx

import mu.KotlinLogging
import org.apache.poi.ss.usermodel.Cell
import java.util.regex.Pattern

object XlsFormula {


    /** 아무런 수정 없이 정해진 문구 리턴  */
    class XlsFormula01(override val value: String) : XlsCellApply {

        override fun cellApply(excelSheet: ExcelSheet, cell: Cell) {
            cell.cellFormula = value
        }
    }

    /**
     * 사칙 연산이 가능한 템필릿 포뮤라
     * x,y는 현재 좌표를 말한다.
     * X축Y축이 변경되지 않는다면 하드코딩하면 된다. 상대적으로 변경된다면 +3, -2 등 상대경로로 적어주면 된다.
     * ==>   x-1은 현제셀에서 한칸 왼쪽이라는 의미
     * ==> 주의! 엑셀에서 X좌표는 숫자가 아니라 알파벳이기 때문에 x.alpha 같은 변환이 필요하다.
     * ==> 현재 좌표 외에도 상수가 필요하다면 map 형태로 추가할 수 있다.    템플릿.put("size", 2)  이런식
     * XlsFormula02 rate = new XlsFormula02("ROUND( ${(x-1).alpha}${y} / ${(x-2).alpha}${y} * 100 , 2 )");
     * XlsFormula02 sum = new XlsFormula02("SUM( ${x.alpha}${y-size} : ${x.alpha}${y-1})").put("size", 2);
     * ignoreError=true 라면 IF 에러 구문을 자동으로 생성해준다.IFERROR문법을 쓰니 구버전 엑셀에서는 오류가 있는듯.. 난 잘 안나옴
     *
     * XXXX 작성하다 말았다.
     */
    class XlsFormula02(override val value: String) : XlsCellApply { //=========이하 걍 복붙 - 나중에 구현체 만들기

        private val ignoreError = false
        private val defaultValue = "0"

        //==================================================== 템플릿용 파라메터들 ======================================================
        private var x = 0
        private var y = 0
        private var size = 0

        companion object {
            private val EL = Pattern.compile("\\#\\{.+?\\}")
            private val log = KotlinLogging.logger {}
        }

        override fun cellApply(excelSheet: ExcelSheet, cell: Cell) {
            //패턴 적용 전에 x값의 경우 알파벳으로 변경해주는 작업을 거친다. 영 부실하다.
//            var replacedPattern = value
//            val elEach: List<String> = RegEx.findAll(EL, value)
//            for (el in elEach) {
//                if (!el.contains("x")) continue
//                val escaper: String = StringEscapeUtil.escapeRegEx(el)
//                val coreLeft = el.substring(2)
//                val core = coreLeft.substring(0, coreLeft.length - 1)
//                val toAlpha = "#{ T(com.epe.util.text.lib.CharUtil).toXlsAlpha( $core ) }"
//                replacedPattern = replacedPattern!!.replaceFirst(escaper.toRegex(), toAlpha)
//            }
//            x = cell.columnIndex + 1 //1부터 시작하는 엑셀 표현식으로 수정해줌
//            y = cell.rowIndex + 1 //1부터 시작하는 엑셀 표현식으로 수정해줌
//            val furmula: String = SpringElUtil.elFormat(replacedPattern, this)
//            log.trace("replacedPattern : {}", replacedPattern)
//            log.trace("coordinate : {}", this)
//            log.trace(" => furmula : {}", furmula)
//            val formula = if (ignoreError) {
//                "IF(ISERROR($furmula),$defaultValue,$furmula)"
//            } else furmula
            cell.cellFormula = ""
        }
    }
    //	private static final Pattern FOMULA = Pattern.compile("[A-Z]{1,5}[\\d]{1,5}");
    //
    //	/** 엑셀 Formula를 찾아서 next만큼 아래로 내려준다. */
    //	public static String nextRowFormula(String formula,final Number next){
    //		return RegEx.findMatchAndReplace(FOMULA, formula,new Converter<String, String>() {
    //			@Override
    //			public String convert(String cellNumber) {
    //				return StringUtil.plusAsLastNumber(cellNumber, next.intValue());
    //			}
    //		});
    //	}
}
