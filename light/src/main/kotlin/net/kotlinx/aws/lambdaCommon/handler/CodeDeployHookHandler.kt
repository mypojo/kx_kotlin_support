package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.json.gson.GsonData
import org.slf4j.LoggerFactory


/**
 * 코드 드플로이 훅
 */
class CodeDeployHookHandler(
    /**
     * arn 기반으로 트리거
     * ex) myproject-day_update-dev
     * ex) {프로젝트명}_{jobDiv}-{deployment_div}
     * */
    private val block: suspend (lifecycleEventHookExecutionId: String, deploymentId: String) -> Unit
) : LambdaLogicHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        val lifecycleEventHookExecutionId = input[LIFECYCLE_EVENT_HOOK_EXECUTION_ID].str ?: return null
        val deploymentId = input[DEPLOYMENT_ID].str!!

        //여기 실서버 8080용 단위테스트가 들어가야 함
        log.info("코드디플로이 빌드 후크 수신 : $deploymentId $lifecycleEventHookExecutionId")
        block(lifecycleEventHookExecutionId, deploymentId)
        return lifecycleEventHookExecutionId
    }

    companion object {
        const val LIFECYCLE_EVENT_HOOK_EXECUTION_ID = "LifecycleEventHookExecutionId"
        const val DEPLOYMENT_ID = "DeploymentId"
    }

}