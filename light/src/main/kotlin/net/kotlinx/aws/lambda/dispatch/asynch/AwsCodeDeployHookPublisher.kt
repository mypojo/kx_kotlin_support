package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * 코드 드플로이 훅
 * 이벤트브릿지 형식이 아님
 */
class AwsCodeDeployHookPublisher : LambdaDispatch {

    private val log = KotlinLogging.logger {}

    private val bus by koinLazy<EventBus>()

    companion object {
        const val LIFECYCLE_EVENT_HOOK_EXECUTION_ID = "LifecycleEventHookExecutionId"
        const val DEPLOYMENT_ID = "DeploymentId"
    }

    /**
     * arn 기반으로 트리거
     * ex) myproject-day_update-dev
     * ex) {프로젝트명}_{jobDiv}-{deployment_div}
     * */
    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val lifecycleEventHookExecutionId = input[LIFECYCLE_EVENT_HOOK_EXECUTION_ID].str ?: return null
        val deploymentId = input[DEPLOYMENT_ID].str!!

        //여기 실서버 8080용 단위테스트가 들어가야 함
        log.info("코드디플로이 빌드 후크 수신 : $deploymentId $lifecycleEventHookExecutionId")
        return bus.postEvent { AwsCodeDeployHookEvent(lifecycleEventHookExecutionId, deploymentId) }
    }

}

