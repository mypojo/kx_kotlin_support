package net.kotlinx.awscdk.iam

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.ManagedPolicyProps
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.PolicyStatementProps


object IamPolicyAdminUtil {

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
        "scheduler:*", //스케쥴러
        "lakeformation:*", //레이크 포메이션
        "quicksight:*", //퀵사이트 (데이터셋 생성, 갱신 등)
        "bedrock:*", //배드락 배치처리

        "codepipeline:StartPipelineExecution", //코드파이프라인 트리거용 (파이프라인 시작 트리거에 부여해야함). 향후 별도 권한으로 뺄것
        "elasticloadbalancing:*", //코드디플로이가 로드밸런서 교체할때 필요함
        "iam:*", //코드디플로이가  ECS 서비스를 업데이트하려면 IAM 역할을 ECS 태스크에 전달할 수 있는 권한이 필요. 향후 세부조정 필요
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