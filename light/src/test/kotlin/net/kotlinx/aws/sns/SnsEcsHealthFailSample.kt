package net.kotlinx.aws.sns

private val sample = """
{
    "version": "0",
    "id": "51a6f4cb-60bd-b181-8ae8-430a485d95af",
    "detail-type": "ECS Task State Change",
    "source": "aws.ecs",
    "account": "653734769926",
    "time": "2022-08-08T05:07:23Z",
    "region": "ap-northeast-2",
    "resources": [
        "arn:aws:ecs:ap-northeast-2:653734769926:task/sin-web_cluster-dev/7e12d648155541fbb8a922c39b586f82"
    ],
    "detail": {
        "attachments": [
            {
                "id": "281fc8d0-aad3-4592-8f75-cce8d9050499",
                "type": "eni",
                "status": "DELETED",
                "details": [
                    {
                        "name": "subnetId",
                        "value": "subnet-045dadaa579a9d61f"
                    },
                    {
                        "name": "networkInterfaceId",
                        "value": "eni-0e12fcc4ff447b0b4"
                    },
                    {
                        "name": "macAddress",
                        "value": "02:32:7c:f8:22:e2"
                    },
                    {
                        "name": "privateDnsName",
                        "value": "ip-10-102-0-134.ap-northeast-2.compute.internal"
                    },
                    {
                        "name": "privateIPv4Address",
                        "value": "10.102.0.134"
                    }
                ]
            }
        ],
        "attributes": [
            {
                "name": "ecs.cpu-architecture",
                "value": "x86_64"
            }
        ],
        "availabilityZone": "ap-northeast-2a",
        "clusterArn": "arn:aws:ecs:ap-northeast-2:653734769926:cluster/sin-web_cluster-dev",
        "connectivity": "CONNECTED",
        "connectivityAt": "2022-08-08T05:04:18.682Z",
        "containers": [
            {
                "containerArn": "arn:aws:ecs:ap-northeast-2:653734769926:container/sin-web_cluster-dev/7e12d648155541fbb8a922c39b586f82/843b051c-10ee-4020-b3ec-75f8c4c9c6d4",
                "exitCode": 143,
                "lastStatus": "STOPPED",
                "name": "sin-web_container-dev",
                "image": "653734769926.dkr.ecr.ap-northeast-2.amazonaws.com/sin-web:dev",
                "imageDigest": "sha256:73b6a47ec14f5ba795aae787a60fa54dc6c46a2a7e74a2ae0ed44fa4220b6578",
                "runtimeId": "7e12d648155541fbb8a922c39b586f82-3367969491",
                "taskArn": "arn:aws:ecs:ap-northeast-2:653734769926:task/sin-web_cluster-dev/7e12d648155541fbb8a922c39b586f82",
                "networkInterfaces": [
                    {
                        "attachmentId": "281fc8d0-aad3-4592-8f75-cce8d9050499",
                        "privateIpv4Address": "10.102.0.134"
                    }
                ],
                "cpu": "0"
            }
        ],
        "cpu": "1024",
        "createdAt": "2022-08-08T05:04:15.061Z",
        "desiredStatus": "STOPPED",
        "enableExecuteCommand": false,
        "ephemeralStorage": {
            "sizeInGiB": 20
        },
        "executionStoppedAt": "2022-08-08T05:07:00.61Z",
        "group": "service:sin-web_service-dev",
        "launchType": "FARGATE",
        "lastStatus": "STOPPED",
        "memory": "2048",
        "overrides": {
            "containerOverrides": [
                {
                    "name": "sin-web_container-dev"
                }
            ]
        },
        "platformVersion": "1.4.0",
        "pullStartedAt": "2022-08-08T05:04:39.246Z",
        "pullStoppedAt": "2022-08-08T05:05:00.276Z",
        "startedAt": "2022-08-08T05:05:11.677Z",
        "startedBy": "ecs-svc/5211643699352071000",
        "stoppingAt": "2022-08-08T05:06:48.601Z",
        "stoppedAt": "2022-08-08T05:07:23.184Z",
        "stoppedReason": "Task failed ELB health checks in (target-group arn:aws:elasticloadbalancing:ap-northeast-2:653734769926:targetgroup/sin-web-target-dev/ccb1c7ee670051a0)",
        "stopCode": "ServiceSchedulerInitiated",
        "taskArn": "arn:aws:ecs:ap-northeast-2:653734769926:task/sin-web_cluster-dev/7e12d648155541fbb8a922c39b586f82",
        "taskDefinitionArn": "arn:aws:ecs:ap-northeast-2:653734769926:task-definition/sin-web_task_def-dev:26",
        "updatedAt": "2022-08-08T05:07:23.184Z",
        "version": 6
    }
}
""".trimIndent()