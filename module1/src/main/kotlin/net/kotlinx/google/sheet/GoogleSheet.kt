package net.kotlinx.google.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import net.kotlinx.google.GoogleSecret

/**
 * 주의!.
 * 제한적인 값만 사용 가능하다. 색칠놀이는 미리 템플릿화 해놓고 읽기/오버라이드 하는 구조로 사용할것!
 *
 * 탭 이름은 직사각형 형태의 구간을 입력. 형식 맞워야함
 * Sheet1!1:2  <--  라인 1~2
 * 작업!A3:C3  <-- 작업 시트의 A3 부터 C3 까지
 *
 * ###### 제한 ######
 * 최대 로우 수 : 40,000
 */
class GoogleSheet(
    googleSecret: GoogleSecret,
    /** 어플 네임  */
    applicationName: String = "Google Sheet",
) {

    /** 시트 본체  */
    val service: Sheets = Sheets.Builder(googleSecret.transport, googleSecret.jsonFactory, googleSecret.credential).setApplicationName(applicationName).build()

    ///==================================================== 조회 ======================================================
    /**
     * @param  sheetId : 구글 시트의 ID 문자열 https://docs.google.com/spreadsheets/d/spreadsheetId/edit#gid=sheetId
     * @param tabName : 탭의 이름(한글 가능)
     */
    fun load(sheetId: String?, tabName: String?): List<List<Any>> {
        val response: ValueRange =
            service.spreadsheets().values().get(sheetId, tabName).setValueRenderOption("UNFORMATTED_VALUE").execute() //https://developers.google.com/sheets/api/reference/rest/v4/ValueRenderOption
        return response.getValues()
    }
    ///==================================================== 쓰기 ======================================================
    /**
     * 해당 구간에 value만 오버라이드 한다. (컬러같은건 안바뀜)
     */
    fun write(sheetId: String?, range: String?, values: List<List<Any?>?>?) {
        val sheet: Sheets.Spreadsheets.Values = service.spreadsheets().values()
        val body = ValueRange().setValues(values)
        sheet.update(sheetId, range, body).setValueInputOption("USER_ENTERED").execute()
    }

}
