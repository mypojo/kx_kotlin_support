package net.kotlinx.awscdk.cloudwatch

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.cloudwatch.Dashboard
import software.amazon.awscdk.services.cloudwatch.DashboardProps
import software.amazon.awscdk.services.cloudwatch.PeriodOverride

/**
 * 클라우드와치 대시보드
 * */
class CdkDashboard : CdkInterface {

    @Kdsl
    constructor(block: CdkDashboard.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${projectName}-${name}-${suff}"

    var name: String = "dashboard"

    lateinit var dashboard: Dashboard

    fun create(stack: Stack) {
        dashboard = Dashboard(
            stack, logicalName, DashboardProps.builder()
                .dashboardName(logicalName)
                .periodOverride(PeriodOverride.AUTO)
                .build()
        )
    }

    /**
     * 기본적인 대시보드를 구성해준다. 샘플코드 참고용!
     * */
    fun createDefault(stack: Stack, rdsName: String?, ecsClusterNames: List<String>, lambdaNames: List<String>) {
        create(stack)

        val dashboardWidget = CdkDashboardWidget {
            dashboard = this@CdkDashboard.dashboard
        }

        rdsName?.let {
            dashboardWidget.namespace = "AWS/RDS"
            dashboardWidget.dimensionsMap = mapOf(
                "DBClusterIdentifier" to it
            )
            dashboardWidget.create("$it CPU", "CPUUtilization", "average")
            dashboardWidget.create("$it max-connection", "DatabaseConnections", "max")
            //서버리스v2 전용. 실제 과금된 ACU를 보여줌
            dashboardWidget.create("$it ACU (serverless v2)", "ServerlessDatabaseCapacity", "average")
        }

        ecsClusterNames.forEach {
            dashboardWidget.namespace = "ECS/ContainerInsights"
            dashboardWidget.dimensionsMap = mapOf(
                "ClusterName" to it
            )
            dashboardWidget.create("$it CPU", "CpuUtilized", "average")
            dashboardWidget.create("$it MEMORY", "MemoryUtilized", "average")
        }

        lambdaNames.forEach {
            dashboardWidget.namespace = "AWS/Lambda"
            dashboardWidget.dimensionsMap = mapOf(
                "FunctionName" to it
            )
            dashboardWidget.create("$it invocations sum", "Invocations", "sum")
            dashboardWidget.create("$it concurrent max", "ConcurrentExecutions", "max")
        }

    }


}