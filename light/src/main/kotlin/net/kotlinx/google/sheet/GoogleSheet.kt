package net.kotlinx.google.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import net.kotlinx.google.GoogleService
import net.kotlinx.number.StringIntUtil

/**
 * 주의!.
 * 제한적인 값만 사용 가능하다. 색칠놀이는 미리 템플릿화 해놓고 읽기/오버라이드 하는 구조로 사용할것!
 *
 * 탭 이름은 직사각형 형태의 구간을 입력. 형식 맞워야함
 *
 * ###### 제한 ######
 * 최대 로우 수 : 40,000
 *
 * @param sheetId : 구글 시트의 ID 문자열 https://docs.google.com/spreadsheets/d/spreadsheetId/edit#gid=sheetId
 * @param tabName : 탭의 이름(한글 가능)
 */
class GoogleSheet(service: GoogleService, val sheetId: String, val tabName: String) {

    val sheets = service.sheets

    /**
     * 간단히 전부 다 읽는다
     * 참고 https://developers.google.com/sheets/api/reference/rest/v4/ValueRenderOption
     */
    @Suppress("UsePropertyAccessSyntax")
    fun readAll(): List<List<Any>> {
        val sheet = sheets.spreadsheets().values().get(sheetId, tabName)
        val response: ValueRange = sheet.setValueRenderOption("UNFORMATTED_VALUE").execute()
        return response.getValues()
    }

    ///==================================================== 쓰기 ======================================================
    /**
     * 해당 구간에 value만 오버라이드 한다. (컬러같은건 안바뀜)
     */
    fun write(values: List<List<Any>>, startAt: Pair<Int, Int> = 1 to 1) {
        val range = GoogleSheetRange {
            this.tabName = this@GoogleSheet.tabName
            this.startAt = startAt
            fromTable(values.first().size - 1 to values.size - 1)
        }
        val sheet: Sheets.Spreadsheets.Values = sheets.spreadsheets().values()
        val body = ValueRange().setValues(values)
        sheet.update(sheetId, range.toRangeString(), body).setValueInputOption("USER_ENTERED").execute()
    }

    /**
     * 가로세로를 구글 시트에서 사용되는 좌표문자로 바꿔준다.
     * ex) Sheet1!1:2  <--  라인 1~2
     * ex) 작업!A3:C3  <-- 작업 시트의 A3 부터 C3 까지
     *  */
    fun Pair<Int, Int>?.toRange(): String {
        if (this == null) return ""
        return "${StringIntUtil.intToUpperAlpha(this.first)}:${this.second}"
    }

}


