package net.kotlinx.module.aws.lambda.snsHandler

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.core.gson.GsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 매칭 실패시, 전송데이터를 그대로 던짐
 * */
class LambdaSnsMatchFail : (GsonData) -> String?, KoinComponent {

    private val eventBus: EventBus by inject()

    override fun invoke(sns: GsonData): String {
        eventBus.post(LambdaSnsEvent("unkown SNS Notification", sns.toString()))
        return LambdaUtil.Ok
    }
}