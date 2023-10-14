package net.kotlinx.excel


import org.apache.poi.ss.usermodel.*

data class XlsStyleSet(
    val left: CellStyle,
    val center: CellStyle,
    val right: CellStyle,
)

/**
 * 미리 정의된 스타일 세트
 */
class ExcellStyle(excel: Excel, val excellFont: ExcellFont) {

    val wb = excel.wb

    /** Header에 사용되는 스타일  */
    val header: CellStyle = create {
        fillForegroundColor = IndexedColors.YELLOW.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        alignment = HorizontalAlignment.CENTER
        wrapText = true
    }


    /** 일반  */
    val normal = XlsStyleSet(
        create {
            alignment = HorizontalAlignment.LEFT
        },
        create {
            alignment = HorizontalAlignment.CENTER
        },
        create(),
    )

    /** 하이라이트 마커  */
    val green = XlsStyleSet(
        create {
            fillForegroundColor = IndexedColors.BRIGHT_GREEN1.index //형광섹 그린
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
        },
        create {
            fillForegroundColor = IndexedColors.BRIGHT_GREEN1.index //형광섹 그린
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
        },
        create {
            fillForegroundColor = IndexedColors.BRIGHT_GREEN1.index //형광섹 그린
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.RIGHT
        },
    )


    /** 퍼센트 스타일 */
    val percentage: CellStyle = create {
        dataFormat = wb.createDataFormat().getFormat("0.00%")
    }

    val redRight: CellStyle = create {
        setFont(excellFont.red)
    }

    val buleRight: CellStyle = create {
        setFont(excellFont.blue)
    }

    /** 기본정보가 등록되는 스타일 빌더 */
    fun create(block: CellStyle.() -> Unit = {}): CellStyle {
        return wb.createCellStyle().apply {
            boxing(this)
            this.setFont(excellFont.normal)
            verticalAlignment = V_CENTER
            alignment = HorizontalAlignment.RIGHT
            block(this)
        }
    }

    companion object {

        /** 세로 중앙정렬 */
        val V_CENTER = VerticalAlignment.forInt(1)!!

//        /** 가로 좌측정렬 */
//        val H_LEFT = HorizontalAlignment.forInt(1)!!
//
//        /** 가로 중앙정렬 */
//        val H_CENTER = HorizontalAlignment.forInt(2)!!
//
//        /** 가로 우측정렬 */
//        val H_RIGHT = HorizontalAlignment.forInt(3)!!

        /**
         * 스타일에 박스테두리 삽입
         */
        private fun boxing(style: CellStyle) {
            //cellStyle.setWrapText( true ); //- 박스안에 다넣기
            style.borderBottom = BorderStyle.THIN
            style.bottomBorderColor = IndexedColors.BLACK.index
            style.borderLeft = BorderStyle.THIN
            style.leftBorderColor = IndexedColors.BLACK.index
            style.borderRight = BorderStyle.THIN
            style.rightBorderColor = IndexedColors.BLACK.index
            style.borderTop = BorderStyle.THIN
            style.topBorderColor = IndexedColors.BLACK.index
        }
    }
}
