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
        get() = "${project.projectName}-${name}-${deploymentType}"

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
    fun createDefault(stack: Stack, rds: String?, ecs: String?, lambda: String?) {
        create(stack)

        val dashboardWidget = CdkDashboardWidget {
            dashboard = this@CdkDashboard.dashboard
        }

        rds?.let {
            dashboardWidget.namespace = "AWS/RDS"
            dashboardWidget.dimensionsMap = mapOf(
                "DBClusterIdentifier" to it
            )
            dashboardWidget.create("RDB CPU", "CPUUtilization", "average")
            dashboardWidget.create("RDB max-connection", "DatabaseConnections", "max")
        }

        ecs?.let {
            dashboardWidget.namespace = "ECS/ContainerInsights"
            dashboardWidget.dimensionsMap = mapOf(
                "ClusterName" to it
            )
            dashboardWidget.create("ECS CPU", "CpuUtilized", "average")
            dashboardWidget.create("ECS MEMORY", "MemoryUtilized", "average")
        }

        lambda?.let {
            dashboardWidget.namespace = "AWS/Lambda"
            dashboardWidget.dimensionsMap = mapOf(
                "FunctionName" to it
            )
            dashboardWidget.create("Lambda invocations sum", "Invocations", "sum")
            dashboardWidget.create("Lambda concurrent max", "ConcurrentExecutions", "max")
        }


    }


}