package net.kotlinx.aws.lambda.dispatch.asynch

import net.kotlinx.aws.AwsNaming
import net.kotlinx.json.gson.GsonData

/**
 * 기본 이벤트브릿지 구조
 * 이를 기반으로 각 객체는 핵심 설정 내용을 코딩해준다
 * 잘 아렬지지 않은경우 이거 그대로 리턴
 *  */
data class EventBridgeJson(override val body: GsonData) : EventBridge {

    override val detailType = body[AwsNaming.EventBridge.DETAIL_TYPE].str ?: body[AwsNaming.EventBridge.DETAIL_TYPE_SNS].str!!

    override val account: String = body["account"].str!!
    override val region: String = body["region"].str!!
    override val time: String = body["time"].str!!
    override val source: String = body["source"].str!!
    override val resources: List<String> = body["resources"].map { it.str!! }

    override val detail: GsonData = body["detail"]

}

