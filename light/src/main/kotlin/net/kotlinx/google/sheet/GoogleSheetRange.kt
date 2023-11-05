package net.kotlinx.google.sheet

import net.kotlinx.core.number.StringIntUtil

/**
 * 구글 시트에서 사용되는 좌표 생성기
 * 가로세로를 구글 시트에서 사용되는 좌표문자로 바꿔준다.
 * ex) Sheet1!1:2  <--  라인 1~2
 * ex) 작업!A3:C3  <-- 작업 시트의 A3 부터 C3 까지
 * */
class GoogleSheetRange(block: GoogleSheetRange.() -> Unit = {}) {

    /** 탭 이름 */
    lateinit var tabName: String

    /** 시작 지점 */
    var startAt: Pair<Int, Int> = 1 to 1

    /** 종료 지점 */
    lateinit var endAt: Pair<Int, Int>

    /** 데이터의 가로세로 크기 기준으로 종료지점 계산 */
    fun fromTable(tableSize: Pair<Int, Int>) {
        endAt = startAt.first + tableSize.first to startAt.second + tableSize.second
    }

    init {
        block(this)
    }

    /** 문자열 변환 */
    fun toRangeString(): String = "${tabName}!${StringIntUtil.intToUpperAlpha(startAt.first)}${startAt.second}:${StringIntUtil.intToUpperAlpha(endAt.first)}${endAt.second}"

}


