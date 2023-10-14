package net.kotlinx.aws.lambdaCommon

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.core.gson.GsonData

/** 간단 핸들러 런타임 typealias */
typealias LambdaLogicHandler = suspend (input: GsonData, context: Context?) -> Any?