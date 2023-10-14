package net.kotlinx.aws.lambda.snsHandler

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module

class LambdaSnsHandlerTest : TestRoot(), KoinComponent {

    class EventListener {
        @Subscribe
        fun basicAwsEvent(event: LambdaSnsEvent) {
            log.info { "${event.title} -> ${event.msg}" }
        }
    }

    @Test
    fun test() {

        startKoin {
            val eventBus = EventBus()
            eventBus.register(EventListener())
            modules(module {
                single<EventBus> { eventBus }
            })
        }

        val input = GsonData.parse(msg)
        log.debug { "input : $input" }
        LambdaSnsHandler()(input)
    }


    val msg = """
    {
        "Records": [
        {
            "EventSource": "aws:sns",
            "EventVersion": "1.0",
            "EventSubscriptionArn": "arn:aws:sns:ap-northeast-2:463327615611:sin-topic-admin_all-dev:6b1303af-e763-49ee-bd8e-c18d82b47640",
            "Sns": {
            "Type": "Notification",
            "MessageId": "54114bbe-ad1a-5924-affe-177f884fbe67",
            "TopicArn": "arn:aws:sns:ap-northeast-2:463327615611:sin-topic-admin_all-dev",
            "Message": "{\"version\":\"0\",\"id\":\"d43b45a8-ea4f-c10a-6cc3-89bce22abc08\",\"detail-type\":\"Step Functions Execution Status Change\",\"source\":\"aws.states\",\"account\":\"463327615611\",\"time\":\"2023-04-25T00:47:17Z\",\"region\":\"ap-northeast-2\",\"resources\":[\"arn:aws:states:ap-northeast-2:463327615611:execution:sin-rpt_demo-dev:dd0c252d-4f2a-b727-5e9a-43773b55a06a\"],\"detail\":{\"executionArn\":\"arn:aws:states:ap-northeast-2:463327615611:execution:sin-rpt_demo-dev:dd0c252d-4f2a-b727-5e9a-43773b55a06a\",\"stateMachineArn\":\"arn:aws:states:ap-northeast-2:463327615611:stateMachine:sin-rpt_demo-dev\",\"name\":\"dd0c252d-4f2a-b727-5e9a-43773b55a06a\",\"status\":\"FAILED\",\"startDate\":1682383637227,\"stopDate\":1682383637293,\"input\":\"{\\n    \\\"Comment\\\": \\\"Insert your JSON here\\\"\\n}\",\"output\":null,\"inputDetails\":{\"included\":true},\"outputDetails\":null,\"error\":\"States.Runtime\",\"cause\":\"An error occurred while executing the state 'Sample01Job' (entered at the event id #4). The JSONPath '${'$'}.jobOption' specified for the field 'jobOption.${'$'}' could not be found in the input '{\\n    \\\"Comment\\\": \\\"Insert your JSON here\\\"\\n}'\"}}",
            "Timestamp": "2023-04-25T00:47:17.978Z",
            "SignatureVersion": "1",
            "Signature": "frsRPqmj4fpHsZf3qbtj3M4MkOAUJKn+MRvwJijsj26AihASqrgAaHsVP6RzFskqsdDfuSg/oSIjcLMuK4WXtp2gz4dXyFFeliFEV8zsuY9BDqVSEHx9CVP6fBXK3z+z78T6nFsAPGBFPacokglYF+0GwDEF0x6++uqV3lqXphilPH7DNrpu57/Ozwi6QOQyxy9nNZbQe9TpDUZ1lMhpWISDQvp9sKftoHsUhUbiSTDVavpCunC/u6YNh9Dy3I4hm3eJ9t9s6KL15Gewb1x89YgO0vlp/TRkwKoK4U7k/7xTw3f2t67FbwTaQYBt3h1XYxf6IQ7SvsZnO/vbc7Xaqw==",
            "SigningCertUrl": "https://sns.ap-northeast-2.amazonaws.com/SimpleNotificationService-56e67fcb41f6fec09b0196692625d385.pem",
            "UnsubscribeUrl": "https://sns.ap-northeast-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:ap-northeast-2:463327615611:sin-topic-admin_all-dev:6b1303af-e763-49ee-bd8e-c18d82b47640",
            "MessageAttributes": {}
        }
        }
        ]
    }        
    """.trimIndent()

}