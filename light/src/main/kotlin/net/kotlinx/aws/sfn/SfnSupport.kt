package net.kotlinx.aws.sfn

import aws.sdk.kotlin.services.sfn.SfnClient
import aws.sdk.kotlin.services.sfn.listActivities
import aws.sdk.kotlin.services.sfn.listExecutions
import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import aws.sdk.kotlin.services.sfn.model.ListActivitiesResponse
import aws.sdk.kotlin.services.sfn.model.ListExecutionsResponse
import aws.sdk.kotlin.services.sfn.model.StartExecutionResponse
import aws.sdk.kotlin.services.sfn.startExecution
import com.lectra.koson.Koson
import com.lectra.koson.obj
import net.kotlinx.aws.AwsNaming
import net.kotlinx.core.gson.GsonData
import java.util.*

/** 결과 리턴 최대치 */
private const val MAX_RESULTS = 1000

/**
 * 시작 호출 (job 전용)
 * block 을 사용해서 inputJson에 추가 파라메터 입력. ex) 예약 시간 등..
 *  */
suspend fun SfnClient.startExecution(awsId: String, stateMachineName: String, jobOption: Any, block: (Koson.() -> Unit)? = null): StartExecutionResponse {

    val uuid = UUID.randomUUID().toString()
    //원본 잡 옵션에 uuid 추가
    val updatedJobOption = GsonData.parse(jobOption.toString()).apply {
        put(SfnUtil.SFN_ID, uuid)
    }
    val inputJson = obj {
        AwsNaming.JOB_OPTION to updatedJobOption.toString() //잡 옵션은 무조건 텍스트임 (json xx)
        block?.invoke(this)
    }
    return this.startExecution {
        this.input = inputJson.toString()
        this.name = uuid
        this.stateMachineArn = SfnUtil.stateMachineArn(awsId, stateMachineName)
    }
}

/**
 * https://docs.aws.amazon.com/step-functions/latest/dg/concepts-activities.html 설명 참조.
 *  */
suspend fun SfnClient.listActivities(nextToken: String? = null): ListActivitiesResponse = this.listActivities {
    this.nextToken = nextToken
    this.maxResults = MAX_RESULTS
}

/**
 * 진행 히스토리. 실행중인거 포함해서 다 리턴됨
 * ex) 이미 동일한 잡이 진행중인지 확인
 *  */
suspend fun SfnClient.listExecutions(awsId: String, stateMachineName: String, executionStatus: ExecutionStatus, nextToken: String? = null): ListExecutionsResponse {
    return this.listExecutions {
        this.statusFilter = executionStatus
        this.nextToken = nextToken
        this.maxResults = MAX_RESULTS
        this.stateMachineArn = SfnUtil.stateMachineArn(awsId, stateMachineName)
    }
}