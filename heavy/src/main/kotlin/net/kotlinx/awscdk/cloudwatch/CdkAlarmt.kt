package net.kotlinx.awscdk.cloudwatch

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.toCdk
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.cloudwatch.AlarmProps
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator
import software.amazon.awscdk.services.cloudwatch.Metric
import software.amazon.awscdk.services.cloudwatch.actions.SnsAction
import software.amazon.awscdk.services.sns.ITopic
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 클라우드와치 알람
 * */
class CdkAlarmt : CdkInterface {

    @Kdsl
    constructor(block: CdkAlarmt.() -> Unit = {}) {
        apply(block)
    }

    /** DB 명 */
    override val logicalName: String
        get() = "${project.profileName}-${alarmName}-${deploymentType}"


    /** 알람 이름 */
    var alarmName: String = "alert-rds"

    /**
     * 매트릭
     * ex) databaseNameCpu(..)
     *  */
    lateinit var metric: Metric

    /** 알람 보낼곳 */
    lateinit var topic: ITopic

    /** 이거 넘으면 포인트 적립. */
    var threshold: Int = 30


    /**
     * datapointsToAlarm / evaluationPeriods
     * evaluationPeriods : 이 숫자만큼의 기간동안 threshold에 datapointsToAlarm만큼 해당하면 알람 보냄
     * evaluationPeriods 가 많아지면 더 많은 스파이크들이 무시됨
     * ex) 5분주기 2 to 4 ->  20분간 4번 측정해서 2번 넘으면 알람
     *  */
    var condition: Pair<Int, Int> = 2 to 4

    /** 비교자. 기본은 이거보다 이상인경우. */
    val comparisonOperator = ComparisonOperator.GREATER_THAN_THRESHOLD

    /** 데이터베이스와 워크 그룹을 만들어준다 */
    fun create(stack: Stack) {

        val alarm = Alarm(
            stack, logicalName, AlarmProps.builder()
                .alarmName(alarmName)
                .comparisonOperator(comparisonOperator)
                .threshold(threshold)
                .datapointsToAlarm(condition.first)
                .evaluationPeriods(condition.second)
                .actionsEnabled(true)
                .metric(metric)
                .build()
        )
        alarm.addAlarmAction(SnsAction(topic))
    }

    companion object {

        /**
         * 데이터베이스 CPU 체크
         * 서버리스 사용하는경우 활용도가 떨아짐..
         * @param databaseName RDS에 있는 그 이름
         *  */
        fun databaseNameCpu(databaseName: String, duration: Duration = 5.minutes): Metric = Metric.Builder.create()
            .namespace("AWS/RDS")
            .dimensionsMap(
                mapOf(
                    "DBClusterIdentifier" to databaseName
                )
            )
            .metricName("CPUUtilization")
            .statistic("average")
            .period(duration.toCdk())
            .build()!!

    }

}