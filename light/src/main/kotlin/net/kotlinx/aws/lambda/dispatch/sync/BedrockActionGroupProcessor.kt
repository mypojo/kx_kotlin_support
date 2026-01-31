package net.kotlinx.aws.lambda.dispatch.sync

import net.kotlinx.json.gson.GsonData


interface BedrockActionGroupProcessor {

    fun invoke(req: BedrockActionGroupReq): GsonData


}
