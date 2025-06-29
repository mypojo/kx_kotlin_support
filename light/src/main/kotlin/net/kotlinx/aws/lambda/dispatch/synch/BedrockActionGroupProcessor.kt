package net.kotlinx.aws.lambda.dispatch.synch

import net.kotlinx.json.gson.GsonData


interface BedrockActionGroupProcessor {

    fun invoke(req: BedrockActionGroupReq): GsonData


}
