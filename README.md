# 소개

- 🛠 유틸리티 도구 모음
- ☁️ AWS 서비스 클라이언트 확장
- 🏗 AWS CDK DSL 지원
- 🔌 외부 API 연동 (Google, Notion 등)
- 🍃 Spring & Hibernate 활용 예제

[![License](https://img.shields.io/badge/license-MIT-blue)](https://opensource.org/license/mit-0/)

## AWS kinesis 실시간 대량 처리 - 요청

```kotlin
val task = KinesisTask {
    streamName = workerStream
    checkpointTableName = "system-dev"
    taskName = "demoTaskJob"
    checkpointTtl = 1.hours
}
val file: File by ResourceHolder.WORKSPACE.slash("largeFile.csv") lazyLoad "s3://xxxa/demo/largeFile.csv"
val flow = file.toInputResource().toFlow()
    .map { line ->
        json {
            "id" to line[0]
            "query" to line[1]
        }
    }
    .chunked(1000)
task.execute(flow).collect { datas ->
    datas.forEach {
        log.debug { " => [${it}]" }
    }
}
```

## AWS kinesis 실시간 대량 처리 - 워커

```kotlin
val worker = KinesisWorker {
    streamName = workerStream
    checkpointTableName = "system-dev"
    handler = { records ->
        log.info { "워커 테스트: ${records.size}개의 레코드 처리" }
        records.forEach {
            it.result.put("processed", true)
            it.result.put("time", java.time.LocalDateTime.now().toKr01())
            log.debug { " -> ${it.result}" }
            100.milliseconds.delay() //0.1초에 1개씩 처리
        }
    }
    readChunkCnt = 100
    shardCheckInterval = 10.minutes
}
worker.start()
```

## AWS CDK - CICD (깃헙 & 코드파이프라인)

```kotlin
val stack = this
val infra = koin<MyInfra>()
val workBucket = infra.s3.work.load(stack)
val appRole = MyRole.APP_ADMIN.load(stack)
val securityGroup = MySecurityGroup.JOB.load(stack)
val toAdmin = infra.topic.adminAll.load(stack)

val build = CdkCodeBuild {
    chacheBucket = workBucket.iBucket
    role = appRole.iRole
    vpc = infra.vpc.iVpc
    securityGroups = listOf(securityGroup.iSecurityGroup)
    concurrentBuildLimit = 1 //AWS 오류..
    gradleVersion = "8.12.1"
    gradleCmds(":deployAll")
    byGithub(MyProject.GITHUB_ROOT, MyProject.PROJECT_DMP)
    create(stack)
}

CdkCodePipeline {
    codeBuild = build.codeBuild
    role = appRole.iRole
    topics = listOf(toAdmin)
    events = when (deploymentType) {
        DeploymentType.PROD -> listOf(EventSets.CodekPipeline.FAILED) //후킹이 걸려있기 때문에 빌드 성공은 필요없음
        DeploymentType.DEV -> listOf(EventSets.CodekPipeline.FAILED, EventSets.CodekPipeline.SUCCESSED)
    }
    byGithub(MyProject.GITHUB_ROOT, MyProject.PROJECT_DMP, "arn:aws:codeconnections:ap-northeast-2:xxxx")
    create(stack)
}
```

## AWS CDK - ECS (블루그린배포)

```kotlin
val infra = koin<MyInfra>()
val ecr = infra.ecr.api.load(stack)

val webConfig = MyEcs.ECS_CONFIGS[CdkInterface.DEPLOYMENT_TYPE]!!
val web = CdkEcsWeb {
    name = "api"
    config = webConfig
    taskRole = MyRole.APP_ADMIN.load(stack).iRole
    executionRole = MyRole.ECS_TASK.load(stack).iRole
    image = ecr.imageFromStackByTag(deploymentType.name.lowercase())
    vpc = infra.vpc.load(stack).iVpc
    sgWeb = MySecurityGroup.API.load(stack).iSecurityGroup
    sgAlb = MySecurityGroup.ALB.load(stack).iSecurityGroup
    containerInsights = deploymentType == DeploymentType.PROD
    environment += mapOf(
        AwsNaming.Spring.ENV_PROFILE to "default,${CdkInterface.SUFF}"
    )
    certs = listOf(MySms.CERT_DMP.get(stack))
    healthCheck = HealthCheck.builder()
        .interval(20.seconds.toCdk())
        .timeout(10.seconds.toCdk())
        .healthyThresholdCount(2) //디폴트인 5로 하면 체크 전에 내려갈 수 있음.
        .unhealthyThresholdCount(2)
        .path("/api/healthcheck")
        .build()

    when (CdkInterface.DEPLOYMENT_TYPE) {
        DeploymentType.PROD -> createServiceBlueGreen(stack)  //라이브서버는 블루그린 배포
        DeploymentType.DEV -> createServiceRolling(stack)
    }
    cdkLogGroup.addLogAnomalyDetector(stack)
}

//도메인 등록하기
val hostedZone = HostedZoneUtil.load(stack, "xxx.com")
val domain = MyEcs.DOMAINS[CdkInterface.DEPLOYMENT_TYPE]!!
Route53Util.arecord(stack, hostedZone, domain, web.alb.toRecordTarget())
```

## AWS CDK - 대량데이터 분할처리기 (SFN)

```kotlin
CdkSfn(project, "batch_step") {
    this.lambda = func
    this.iRole = role.iRole

    val stepStart = lambda("StepStart")
    val stepEnd = lambda("StepEnd")

    val modeMap = listOf(
        mapInline("StepMap") {
            next = stepEnd.stateId
            itemPath = "$.option.${stepStart.stateId}.body.datas"
        },
        stepEnd,
    ).join()

    val listMode = run {
        val stepList = lambda("StepList")
        val waitColdstart = wait("WaitColdstart") {
            this.secondsPath = "${AwsNaming.option}.${AwsNaming.waitColdstartSeconds}"
        }
        val waitIpBlock = wait("WaitIpBlock") {
            this.secondsPath = "${AwsNaming.option}.${AwsNaming.waitSeconds}"
        }
        listOf(
            stepList,
            choice("IsCompleted").apply {
                whenMatchesBody(stepList.stateId, AwsNaming.choiceFirst, waitColdstart, stepList)
                whenMatchesBody(stepList.stateId, AwsNaming.choiceRetry, waitIpBlock, stepList)
                otherwise(stepEnd)
            },
        ).join()
    }

    create(
        stepStart,
        choice("WhenMode").apply {
            whenMatches("mode", "List", listMode)
            otherwise(modeMap)
        },
    )
    onErrorHandle(adminAllTopic, dlq.iQueue)
}
```