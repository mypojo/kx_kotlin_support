package net.kotlinx.awscdk.cloudwatch

import net.kotlinx.awscdk.toCdk
import net.kotlinx.collection.RoundRobin
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.services.cloudwatch.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 클라우드와치 대시보드 위젯
 * */
class CdkDashboardWidget {

    @Kdsl
    constructor(block: CdkDashboardWidget.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 전체 고정 ======================================================
    var width: Int = 8
    var height: Int = 5
    var period: Duration = 15.minutes
    var colors = RoundRobin(listOf("#1f77b4", "#ff7f0e", "#2ca02c", "#ff9896"))

    /** 추가할 대시보드 */
    lateinit var dashboard: Dashboard

    //==================================================== 그룹별로 고정 ======================================================
    /** 그룹 */
    lateinit var namespace: String

    /** 디멘션 */
    lateinit var dimensionsMap: Map<String, String>

    /** 개별 위젯 그리기 */
    fun create(title: String, metricName: String, statistic: String) {
        val widget = GraphWidget(
            GraphWidgetProps.builder()
                .width(width)
                .height(height)
                .title(title)
                .view(GraphWidgetView.TIME_SERIES)
                .stacked(true) //스택이 좀더 보기 좋은듯
                .left(
                    listOf(
                        Metric.Builder.create()
                            .namespace(namespace)
                            .metricName(metricName)
                            .dimensionsMap(dimensionsMap)
                            .statistic(statistic)
                            .period(period.toCdk())
                            .color(colors.next())
                            .build()!!
                    )
                )
                .build()
        )
        dashboard.addWidgets(widget)
    }

}