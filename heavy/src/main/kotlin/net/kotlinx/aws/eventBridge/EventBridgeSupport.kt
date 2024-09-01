package net.kotlinx.aws.eventBridge

import aws.sdk.kotlin.services.eventbridge.EventBridgeClient
import aws.sdk.kotlin.services.eventbridge.listRules
import aws.sdk.kotlin.services.eventbridge.model.ListRulesResponse
import aws.sdk.kotlin.services.eventbridge.model.PutEventsRequestEntry
import aws.sdk.kotlin.services.eventbridge.model.PutEventsResponse
import aws.sdk.kotlin.services.eventbridge.putEvents

/**
 * 시간입력은 따로 하지 않고 자체 채번 시간사용 (비교용)
 * https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-putevent-size.html
 * 총 항목의 크기가 256 kb 이하만 가능 -> 더 크다면 S3를 권장함
 * *   */
suspend fun EventBridgeClient.putEvents(config: EventBridgeConfig, datas: List<String>): PutEventsResponse {
    return this.putEvents {
        this.entries = datas.map { data ->
            PutEventsRequestEntry {
                this.eventBusName = config.eventBusName
                this.source = config.source
                this.detailType = config.detailType
                this.resources = config.resources
                this.detail = data
            }
        }
    }
}

suspend fun EventBridgeClient.listRules(eventBusName: String, nextToken: String? = null): ListRulesResponse = this.listRules {
    this.eventBusName = eventBusName
    this.nextToken = nextToken
}