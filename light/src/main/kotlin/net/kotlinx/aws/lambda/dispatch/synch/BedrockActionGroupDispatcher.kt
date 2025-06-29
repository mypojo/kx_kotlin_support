package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.json
import net.kotlinx.koin.Koins.koinLazy


/**
 * 배드락 에이전트에서 액션 그룹 호출시 디스패쳐
 */
class BedrockActionGroupDispatcher : LambdaDispatch {

    private val actionGroupProcessor by koinLazy<BedrockActionGroupProcessor>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {

        input["actionGroup"].str ?: return null

        val req = BedrockActionGroupReq(input)

        val body = actionGroupProcessor.invoke(req)
        val result = json {
            "response" to obj {
                "actionGroup" to req.actionGroup
                "apiPath" to req.apiPath
                "httpMethod" to req.httpMethod
                "httpStatusCode" to 200
                "responseBody" to obj {
                    "application/json" to obj {
                        "body" to rawJson(body.toString())
                    }
                }
            }
            "messageVersion" to req.messageVersion
        }
        return result
    }

}
