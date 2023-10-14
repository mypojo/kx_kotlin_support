package net.kotlinx.spring.el

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.time.TimeFormat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SpringElUtilTest : TestRoot() {

    internal data class Item(val name: String, val cost: Long) {
        var code: String? = null
        var map: Map<String, String>? = null
        var startTime: LocalDateTime? = null
        var endTime: LocalDateTime? = null

        //==================================================== 커스텀 ======================================================
        val startDate: String
            get() = TimeFormat.Y2MD_K01[startTime]
        val endDate: String
            get() = TimeFormat.Y2MD_K01[endTime]

    }

    @Test
    fun test() {

        val item = Item("자전거", 900).apply {
            code = "A786"
            map = mapOf("바퀴" to "정상", "카메라" to "없어요")
            startTime = LocalDateTime.now().minusDays(20)
            endTime = LocalDateTime.now()
        }

        val templateTexts = listOf(
            "가격 = #{cost+200}  기간 :  #{startDate} ~ #{endDate}",
            "가격검수 -> #{cost > 700 ? '합격' : '불합격'}",
            "상품명은 #{name} 입니다. 카메라 = #{map['카메라']}",
            "구내건수 = #{1+3}",
            "바이트수 : #{'Hello World'.bytes.length} -> #{'abc'.substring(2, 3)}",
            "랜덤숫자 #{T(java.lang.Math).random() * 100 + 1}",
            "엑셀시트 #{ T(net.kotlinx.core.number.StringIntUtil).INSTANCE.intToUpperAlpha( 2 ) }", //컴퍼니언 오브젝트는 INSTANCE를 붙여야함
        )

        val datas = templateTexts.map { arrayOf(it, SpringElUtil.elFormat(it, item)) }.toList()

        listOf("템플릿", "결과").toTextGrid(datas).print()

        log.info { " -> 텍스트 추출값 ${SpringElUtil.extract<String>("code", item)}" }
    }

}