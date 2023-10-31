package net.kotlinx.aws.sfn

import aws.sdk.kotlin.services.sfn.*
import aws.sdk.kotlin.services.sfn.model.DescribeExecutionResponse
import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import aws.sdk.kotlin.services.sfn.model.ListActivitiesResponse
import aws.sdk.kotlin.services.sfn.model.ListExecutionsResponse

/** 결과 리턴 최대치 */
private const val MAX_RESULTS = 1000

/** SFN 실행 샘플 */
suspend fun SfnClient.startExecution(awsId: String, stateMachineName: String, sfnId: String, json: Any) {
    this.startExecution {
        this.input = json.toString()
        this.name = sfnId
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

/** 샘플 참고용 */
suspend fun SfnClient.describeExecution(executionArn: String): DescribeExecutionResponse {
    return this.describeExecution {
        this.executionArn = executionArn
    }
}