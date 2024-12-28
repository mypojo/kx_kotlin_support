package net.kotlinx.awscdk.iam

/**
 * 자주 사용되는 Role 샘플
 * */
object CdkIamRoleSet {

    /** 앱 실행용 관리자 권한 */
    val APP_ADMIN = CdkIamRole {
        roleName = "app-admin"
        services = CdkIamService.entries.map { it.serviceName }
        managedPolicy(CdkManagedPolicySet.SCHEDULER)
    }

    /** ECS Task 실행용 */
    val ECS_TASK = CdkIamRole {
        roleName = "app-ecs_task"
        services = listOf(CdkIamService.ECS_TASK.serviceName)
        managedPolicy(CdkManagedPolicySet.ECS)
    }

    /** 배스천용 역할 */
    val EC2_SSM = CdkIamRole {
        roleName = "app-ec2_ssm"
        services = listOf("ec2.amazonaws.com")
        managedPolicy(CdkManagedPolicySet.SSM)
    }

    /**
     * https://docs.aws.amazon.com/firehose/latest/dev/controlling-access.html#using-iam-iceberg
     * 아이스버그 호환 역할
     * 편의상 리소스 부분은 생략함
     *
     * 레이크포메이션 사용시 app-firehose  권한(Data lake administrators 등록 등)을 줘야함 => 명시적으로 역할 만드는게 편함
     *  */
    val FIREHOSE: CdkIamRole = CdkIamRole {
        roleName = "app-firehose"
        services = listOf("firehose.amazonaws.com")
        actions = listOf(
            "glue:GetTable",
            "glue:GetTableVersion",
            "glue:GetTableVersions",
            "s3:AbortMultipartUpload",
            "s3:GetBucketLocation",
            "s3:GetObject",
            "s3:ListBucket",
            "s3:ListBucketMultipartUploads",
            "s3:PutObject",
            "s3:DeleteObject",
            "kinesis:DescribeStream",
            "kinesis:GetShardIterator",
            "kinesis:GetRecords",
            "kinesis:ListShards",
            "kms:GenerateDataKey",
            "kms:Decrypt",
            "lambda:InvokeFunction",
            "lambda:GetFunctionConfiguration",
            "logs:PutLogEvents",
        )
    }

    /**
     * 이벤트브릿지 종합 역할
     * ex) 필터 걸어서 파이어호스 호출
     * DQL 설정했더라도 SQS 권한은 안줘도 됨..
     * CDK에서 null로 넣으면 보통 자동으로 생성됨 => 쓸모는 없음
     * */
    val EVENTS: CdkIamRole = CdkIamRole {
        roleName = "app-events"
        services = listOf("events.amazonaws.com")
        actions = listOf(
            "firehose:PutRecord",
            "firehose:PutRecordBatch",
        )
    }

}
