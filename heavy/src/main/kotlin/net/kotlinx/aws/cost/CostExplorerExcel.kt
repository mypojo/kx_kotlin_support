package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.GroupDefinitionType
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.collection.flattenAny
import net.kotlinx.core.Kdsl
import net.kotlinx.excel.Excel
import net.kotlinx.excel.XlsComment
import net.kotlinx.excel.XlsFormula
import net.kotlinx.number.StringIntUtil
import net.kotlinx.string.ifEmpty
import net.kotlinx.time.TimeFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * 간단 엑셀 출력기
 * */
class CostExplorerExcel {

    @Kdsl
    constructor(block: CostExplorerExcel.() -> Unit = {}) {
        apply(block)
    }

    private val log = KotlinLogging.logger {}

    //==================================================== 설정 ======================================================
    /** 단위(월)별 개별 금액이 금액 넘는거만 시트에 상세 표시. 단위 : 달라  */
    var limitCost = 0.5

    /**
     * 달러-원 환율
     * @see net.kotlinx.okhttp.OkHttpSamples.dollarWonAwait
     *  */
    var won = AwsConfig.EXCHANGE_RATE

    /** 수수료 적용 (메가존, 다우데이터 등등..) */
    var fee = 1.1

    /** 부가세 적용 */
    var tax = 1.1

    /** API 결과 데이터 입력 */
    lateinit var costDatas: List<CostExplorerLine>

    //==================================================== 내부사용 ======================================================

    /** 파일로 쓸때는 이걸 꺼내서 사용할것  */
    val excel: Excel = Excel()

    /** 전체 타임시리즈 정보 */
    val totalMonths by lazy { costDatas.map { it.timeSeries!! }.distinct().sortedBy { it } }

    /** 프로젝트별 정보 */
    val groupByProject by lazy { costDatas.groupBy { it.projectName } }

    /** 최종 계산 */
    fun Double.toWon(): BigDecimal = (this * won * fee * tax / 10000).toBigDecimal().setScale(1, RoundingMode.HALF_UP) //보기 편하게 반올림

    /** 헤더에 상세 설명 추가 */
    fun List<Any>.convertHeader(): List<Any> {
        return this.map {
            if (it is String) {
                when (it) {
                    "EC2-Other" -> XlsComment(it) {
                        comments = listOf(
                            "포함되는 비용(아래 리스트)",
                            "EBS(디스크마운트,",
                            "NAT",
                            "Elastic IP Addresses",
                        )
                    }

                    "AWS Key Management Service" -> XlsComment("KMS") {
                        comments = listOf(
                            it,
                            "AWS의 시크릿키 보관 서비스",
                            "각 키당 월 1$",
                        )
                    }

                    "EC2 Container Registry (ECR)" -> XlsComment("ECR") {
                        comments = listOf(
                            it,
                            "AWS의 컨테이너 Registry 서비스",
                        )
                    }

                    "Elastic Container Service" -> XlsComment("ECS") {
                        comments = listOf(
                            it,
                            "AWS의 컨테이너 컴퓨팅 서비스",
                        )
                    }

                    else -> it
                }
            } else {
                it
            }
        }
    }

    /** AWS 계정을 월별로 전체 보기  */
    fun groupByProject() {

        //전체 키값(헤더)을 얻어냄
        val sheet = excel.createSheet("종합")
        sheet.addHeader(
            listOf(
                "프로젝트 명",
                totalMonths.take(totalMonths.size - 1),
                XlsComment(totalMonths.last()) {
                    comments = listOf(
                        "이 구간은 당월 구간입니다.",
                        "아직 전체 비용이 발생하지 않았음으로 비용이 적게 측정됩니다.",
                    )
                },
                XlsComment("합계(만원)") {
                    comments = listOf(
                        "달러원환율(${won}) / 수수료(${fee}) / 부가세(${tax})가 포함된 금액입니다.",
                        "편의상 작은 금액(${limitCost}$)은 미포함임으로",
                        "실제 금액보다 조금 적은 금액이 표기됩니다",
                    )
                },
                "합산비용비율(%)",
            ).flattenAny()
        )

        val startCol = StringIntUtil.intToUpperAlpha(2)
        val endCol = StringIntUtil.intToUpperAlpha(totalMonths.size + 1)
        val sumCol = StringIntUtil.intToUpperAlpha(totalMonths.size + 1 + 1)
        val gridEndRowNum = groupByProject.size + 1
        groupByProject.entries.forEachIndexed { i, e ->
            val groupByTime = e.value.filter { it.groupDefinitionType == GroupDefinitionType.Dimension.value }.groupBy { it.timeSeries }
            val values = totalMonths.map { time -> groupByTime[time]?.sumOf { it.costValue!! }?.toWon() ?: 0 }
            val row = i + 1 + 1 //헤더 + 0부터 시작
            val line = listOf(
                e.key!!,
                values,
                XlsFormula("ROUND(SUM(${startCol}${row}:${endCol}${row}) ,0)"), //합계
                XlsFormula("ROUND(${sumCol}${row}/SUM(${sumCol}${2}:${sumCol}$gridEndRowNum),4)") { style = sheet.excel.style.percentage }  //증감 퍼센트
            ).flattenAny()
            sheet.writeLine(line)
        }

        //합계 로우
        val totalCol = StringIntUtil.intToUpperAlpha(totalMonths.size + 1 + 1 + 1)
        sheet.writeLine(
            listOf(
                "합계",
                (0..totalMonths.size).map {
                    val col = StringIntUtil.intToUpperAlpha(it + 2)
                    XlsFormula("ROUND(SUM( ${col}${2}:${col}${gridEndRowNum}) ,0)")
                },
                XlsFormula("SUM( ${totalCol}${2}:${totalCol}${gridEndRowNum})") {
                    style = sheet.excel.style.percentage
                }
            ).flattenAny()
        )
        //합계의 증감 퍼센트
        sheet.writeLine(
            listOf(
                "증감",
                "-",
                (1..(totalMonths.size - 1)).map {
                    val col1 = StringIntUtil.intToUpperAlpha(it + 2)
                    val col2 = StringIntUtil.intToUpperAlpha(it + 1)
                    val currentRow = gridEndRowNum + 1
                    XlsFormula("ROUND((${col1}${currentRow}-${col2}${currentRow})/${col2}${currentRow}*100,2)")
                },
                "-",
                "-",
            ).flattenAny()
        )
    }

    /** 각 AWS 별 상세 정보 저장  */
    fun eachProject() {

        val rptConfigs = mapOf(
            GroupDefinitionType.Dimension to "서비스별",
            GroupDefinitionType.Tag to "태그별",
        )

        groupByProject.entries.forEach { entry ->
            rptConfigs.forEach { config ->
                eachProjectWrite(config, entry)
            }
        }

    }

    private fun eachProjectWrite(config: Map.Entry<GroupDefinitionType, String>, entry: Map.Entry<String?, List<CostExplorerLine>>) {
        log.debug { " -> ${config.value}별 시트 생성.." }

        //항목이 너무 많기때문에 1달러 안되는애들은 무시
        val targeteList: List<CostExplorerLine> = entry.value.filter { it.groupDefinitionType == config.key.value }.filter { it.costValue!! >= limitCost }
        val keyNames = targeteList.map { it.key!! }.distinct().sorted().toList()
        if (keyNames.isEmpty()) return //태그기반 리포트 등은 없을 수 있음

        val sheet = excel.createSheet("${entry.key}_${config.value}")
        sheet.addHeader(
            listOf(
                "Month",
                //헤더 네임은 태그 접두어 제거
                keyNames.map { key ->
                    when {
                        key.contains("$") -> key.substringAfter("$").ifEmpty { "-" }
                        else -> key
                    }
                },
                "월비용(만원)",
                "증감(%)",
            ).flattenAny().convertHeader()
        )

        val startCol = StringIntUtil.intToUpperAlpha(2)
        val endCol = StringIntUtil.intToUpperAlpha(keyNames.size + 1)
        val sumCol = StringIntUtil.intToUpperAlpha(keyNames.size + 2)
        val lastMonth = TimeFormat.YM_F01[LocalDate.now().minusMonths(1)]
        totalMonths.forEachIndexed { i, month ->
            val groupByService = targeteList.filter { it.timeSeries == month }.associateBy { it.key!! }

            val values = keyNames.map { v -> groupByService[v]?.costValue?.toWon() ?: 0.0 }
            val row = i + 1 + 1 //헤더 + 0부터 시작

            val isLastMonth = month == lastMonth

            val line = listOf(
                when (isLastMonth) {
                    true -> XlsComment(month) {
                        this.comments = listOf("지난달 데이터")
                    }

                    false -> month
                },
                values,
                XlsFormula("ROUND(SUM(${startCol}${row}:${endCol}${row}) ,0)"),
                if (row <= 2) "" else XlsFormula("ROUND((${sumCol}${row}-${sumCol}${row - 1})/${sumCol}${row - 1}*100,2)") { iferror = true }, //합계비용 증감
            ).flattenAny()

            if (isLastMonth) {
                sheet.customRowStyleSet[row - 1] = sheet.excel.style.green
            }
            sheet.writeLine(line)

        }
    }
}


