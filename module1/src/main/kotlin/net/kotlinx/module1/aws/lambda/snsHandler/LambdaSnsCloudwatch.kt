package net.kotlinx.module1.aws.lambda.snsHandler

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.core2.gson.GsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 클라우드와치 알람
 * 향후 케이스 추가..
 * */
class LambdaSnsCloudwatch : (GsonData) -> String?, KoinComponent {

    private val eventBus: EventBus by inject()

    override fun invoke(sns: GsonData): String? {

        val alarmName = sns["AlarmName"].str ?: return null

        val data = sns["Trigger"]
        if (data.empty) return null

        //케이스 추가 필요
        val msg = "${data["Namespace"]} ${data["MetricName"]}\n${sns["NewStateReason"]}"

        eventBus.post(LambdaSnsEvent(alarmName, msg))
        return LambdaUtil.Ok

    }
}

private const val sample = """
{
  "AlarmName": "sin-alarm-rds-dev",
  "AlarmDescription": null,
  "AWSAccountId": "653734769926",
  "AlarmConfigurationUpdatedTimestamp": "2022-11-08T02:50:15.240+0000",
  "NewStateValue": "ALARM",
  "NewStateReason": "Threshold Crossed: 2 out of the last 4 datapoints [10.708333333333332 (08/11/22 02:40:00), 10.791661891275925 (08/11/22 02:30:00)] were greater than the threshold (10.7) (minimum 2 datapoints for OK -> ALARM transition).",
  "StateChangeTime": "2022-11-08T02:51:18.729+0000",
  "Region": "Asia Pacific (Seoul)",
  "AlarmArn": "arn:aws:cloudwatch:ap-northeast-2:653734769926:alarm:sin-alarm-rds-dev",
  "OldStateValue": "OK",
  "OKActions": [],
  "AlarmActions": [
    "arn:aws:sns:ap-northeast-2:653734769926:sin-topic_admin_all-dev"
  ],
  "InsufficientDataActions": [],
  "Trigger": {
    "MetricName": "CPUUtilization",
    "Namespace": "AWS/RDS",
    "StatisticType": "Statistic",
    "Statistic": "AVERAGE",
    "Unit": null,
    "Dimensions": [
      {
        "value": "sin-dev",
        "name": "DBClusterIdentifier"
      }
    ],
    "Period": 300,
    "EvaluationPeriods": 4,
    "DatapointsToAlarm": 2,
    "ComparisonOperator": "GreaterThanThreshold",
    "Threshold": 10.7,
    "TreatMissingData": "",
    "EvaluateLowSampleCountPercentile": ""
  }
}

{
  "AlarmName": "sin-web_cpu",
  "AlarmDescription": null,
  "AWSAccountId": "331671628331",
  "AlarmConfigurationUpdatedTimestamp": "2022-04-26T01:59:27.218+0000",
  "NewStateValue": "ALARM",
  "NewStateReason": "Threshold Crossed: 1 out of the last 1 datapoints [7.272883842388789 (26/04/22 03:06:00)] was greater than the threshold (5.0) (minimum 1 datapoint for OK -> ALARM transition).",
  "StateChangeTime": "2022-04-26T03:08:00.549+0000",
  "Region": "Asia Pacific (Seoul)",
  "AlarmArn": "arn:aws:cloudwatch:ap-northeast-2:331671628331:alarm:sin-web_cpu",
  "OldStateValue": "OK",
  "OKActions": [],
  "AlarmActions": [
    "arn:aws:sns:ap-northeast-2:331671628331:md-alert"
  ],
  "InsufficientDataActions": [],
  "Trigger": {
    "MetricName": "CPUUtilization",
    "Namespace": "AWS/ECS",
    "StatisticType": "Statistic",
    "Statistic": "AVERAGE",
    "Unit": null,
    "Dimensions": [
      {
        "value": "sin-center-svc",
        "name": "ServiceName"
      },
      {
        "value": "sin_center",
        "name": "ClusterName"
      }
    ],
    "Period": 60,
    "EvaluationPeriods": 1,
    "DatapointsToAlarm": 1,
    "ComparisonOperator": "GreaterThanThreshold",
    "Threshold": 5.0,
    "TreatMissingData": "missing",
    "EvaluateLowSampleCountPercentile": "\"}}"
  }
}
}
"""