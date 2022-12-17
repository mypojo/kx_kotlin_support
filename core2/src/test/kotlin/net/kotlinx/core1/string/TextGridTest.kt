package net.kotlinx.core1.string

import org.junit.jupiter.api.Test

internal class TextGridTest{
    @Test
    fun `기본테스트`(){

        listOf("메뉴명", "설명", "path").toTextGrid(
            listOf(
                arrayOf("연동오류 통합조회","레드시프트로부터 오류 로그 분석함","/errs"),
                arrayOf("사용자 통합조회","RDS 집계분석","/users"),
            )
        ).print()

    }
}