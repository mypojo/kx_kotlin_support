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
 * 256 kb 이하만 가능
 * *   */
suspend fun EventBridgeClient.putEvents(datas: List<EventBridgeData>): PutEventsResponse {
    return this.putEvents {
        this.entries = datas.map { data ->
            PutEventsRequestEntry {
                this.eventBusName = data.eventBusName
                this.source = data.source
                this.detailType = data.detailType
                this.resources = data.resources
                this.detail = data.detail
            }
        }
    }
}

suspend fun EventBridgeClient.listRules(eventBusName: String, nextToken: String? = null): ListRulesResponse = this.listRules {
    this.eventBusName = eventBusName
    this.nextToken = nextToken
}
