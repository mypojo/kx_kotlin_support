package net.kotlinx.aws.lambda.dispatch

import net.kotlinx.json.gson.GsonData

/** 람다로 작동하는 동작들 */
interface LambdaDispatchLogic {

    suspend fun execute(input: GsonData): Any

}