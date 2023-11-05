package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.ManagedPolicyProps
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.PolicyStatementProps


object IamPolicyUtil {

    /** 관리자용 권한 샘플 */
    val DEFAULT_ADMIN_ACTIONS = listOf(
        "logs:*",
        "cloudwatch:*",
        "xray:*",
        "ssm:*",
        "athena:*",
        "sqs:*",
        "sns:*",
        "s3:*",
        "kinesis:*",
        "dynamodb:*",
        "lambda:*",
        "autoscaling:*",
        "glue:*",
        "ecs:*",
        "kms:*",
        "batch:*",
        "rds-db:*",
        "datapipeline:*",
        "ec2:*", //이거 쓸일 거의 없으니 나중에 삭제
        "codecommit:*",
        "codedeploy:*",
        "codebuild:*",
        "sts:*",
        "ecr:*",
        "firehose:*",
        "states:*", //sfn.. 애네 이름에 일관성이 없음.
        "events:*", //이벤트브릿지
        "codepipeline:StartPipelineExecution", //코드파이프라인 트리거용 (파이프라인 시작 트리거에 부여해야함). 향후 별도 권한으로 뺄것
    )

    val ALL = listOf("*")

    /** 간단 폴리시 생성. 주로 관리자 정책 만들때 사용함 */
    fun createDefaultPolicy(stack: Stack, policyName: String = "app_admin", actions: List<String> = DEFAULT_ADMIN_ACTIONS): ManagedPolicy {
        return ManagedPolicy(
            stack, policyName + "_policy", ManagedPolicyProps.builder()
                .managedPolicyName(policyName)
                .description(policyName)
                .statements(
                    listOf(
                        PolicyStatement(
                            PolicyStatementProps.builder().actions(actions).resources(ALL).build()
                        )
                    )
                )
                .build()
        )
    }


}