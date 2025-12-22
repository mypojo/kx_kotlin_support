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

---

## 프로젝트별 패키지 기능 요약

### Core 프로젝트

> 최소한의 의존성을 가진 핵심 유틸리티 모음

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **net.kotlinx.ai** | AI 텍스트 모델 클라이언트 | AI 모델 연동, 텍스트 입출력 처리 |
| **net.kotlinx.calculator** | 배치 처리 진행률 계산 | 대용량 배치 작업의 청크 분할 및 진행률 추적 |
| **net.kotlinx.collection** | 컬렉션 확장 함수 | List/Map/Set 등의 확장 함수 (diff, flattenAny, groupByFirstCondition 등) |
| **net.kotlinx.concurrent** | 동시성 처리 | 코루틴 실행기, 스레드 관리, StopWatch, CacheMap, MicroBatchQueue |
| **net.kotlinx.core** | 핵심 유틸리티 | 클래스 로딩, 데이터 변환, KDSL, 패키지명 처리 |
| **net.kotlinx.counter** | 이벤트 카운터 | 이벤트 횟수/시간 체크, Latch 구현 |
| **net.kotlinx.csv** | CSV 파일 처리 | CSV 읽기/쓰기, 집계, Flow 변환 |
| **net.kotlinx.delegate** | 델리게이트 패턴 | Map 기반 속성 델리게이트 (MapAttribute) |
| **net.kotlinx.domain** | 도메인 모델 | 개발자 정보, 메뉴, 쿼리, 트리 구조 등 공통 도메인 |
| **net.kotlinx.exception** | 예외 처리 | KnownException, 예외 체이닝 유틸리티 |
| **net.kotlinx.file** | 파일 처리 | Gzip/Zip 압축, 파일명 처리, 랜덤 셔플 |
| **net.kotlinx.flow** | Flow 확장 | Kotlin Flow 확장 함수 |
| **net.kotlinx.html** | HTML 생성 | HTML 태그 빌더, htmx 지원 |
| **net.kotlinx.id** | ID 생성기 | GUID 대용량 채번기 (하이/로우 방식) |
| **net.kotlinx.io** | 입출력 리소스 | InputResource/OutputResource 추상화 |
| **net.kotlinx.json** | JSON 처리 | Gson/Koson/JsonPath/Serialization 지원 |
| **net.kotlinx.ksp** | KSP 관련 | Kotlin Symbol Processing 유틸리티 |
| **net.kotlinx.lazyLoad** | 지연 로딩 | 지연 로딩 프로퍼티 구현 |
| **net.kotlinx.logback** | 로깅 | Logback 확장, 임시 로거 |
| **net.kotlinx.number** | 숫자 확장 | Int/Long/Double/Boolean 확장 함수, 숫자 단축 표현 |
| **net.kotlinx.regex** | 정규식 | 정규식 유틸리티 및 확장 |
| **net.kotlinx.retry** | 재시도 로직 | 백오프 지원 재시도 템플릿 |
| **net.kotlinx.string** | 문자열 확장 | 문자열 변환/검증/암호화, 한글 처리, 결과 데이터 래퍼 |
| **net.kotlinx.system** | 시스템 유틸리티 | OS 타입, 배포 타입, 리소스 홀더, 시스템 구분자 |
| **net.kotlinx.time** | 시간/날짜 처리 | LocalDate/LocalDateTime/Duration 확장, 타임존 처리 |
| **net.kotlinx.validation** | 검증 | Bean Validation, Konform, 조건부 검증 |
| **net.kotlinx.xml** | XML 처리 | XML 데이터 파싱 및 처리 |

### Light 프로젝트

> AWS Lambda를 위한 의존성 포함 (AWS 서비스 및 외부 API 연동)

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **net.kotlinx.aws.athena** | Athena 쿼리 | CloudTrail 등 테이블 정의, 쿼리 실행 및 결과 조회 |
| **net.kotlinx.aws.batch** | AWS Batch | 배치 작업 제출 및 관리 |
| **net.kotlinx.aws.bedrock** | Bedrock AI | Claude 등 AI 모델 호출, 프롬프트 관리 |
| **net.kotlinx.aws.codeCommit** | CodeCommit | Git 저장소 연동 |
| **net.kotlinx.aws.cognito** | Cognito | 사용자 풀 관리, 인증/인가 |
| **net.kotlinx.aws.dynamo** | DynamoDB | 테이블 CRUD, Enhanced Client, 멀티 인덱스, Lock 구현 |
| **net.kotlinx.aws.ecs** | ECS | 컨테이너 서비스 관리 |
| **net.kotlinx.aws.eventBridge** | EventBridge | 이벤트 발행 및 구독 |
| **net.kotlinx.aws.fargate** | Fargate | 서버리스 컨테이너 실행 |
| **net.kotlinx.aws.firehose** | Firehose | 실시간 로그 스트리밍 |
| **net.kotlinx.aws.iam** | IAM | 권한 및 역할 관리 |
| **net.kotlinx.aws.kinesis** | Kinesis | 실시간 대량 데이터 처리 (Task/Worker 패턴) |
| **net.kotlinx.aws.lambda** | Lambda | 람다 함수 호출, 디스패치 패턴 (동기/비동기) |
| **net.kotlinx.aws.lambdaFunction** | Lambda 함수 | 람다 함수 생성 및 배포 헬퍼 |
| **net.kotlinx.aws.lambdaUrl** | Lambda URL | 람다 함수 URL 관리 |
| **net.kotlinx.aws.logs** | CloudWatch Logs | 로그 그룹/스트림 관리, 쿼리 |
| **net.kotlinx.aws.rdsdata** | RDS Data API | 서버리스 Aurora 쿼리 실행 |
| **net.kotlinx.aws.s3** | S3 | 파일 업로드/다운로드, 버킷 관리, Flow 지원 |
| **net.kotlinx.aws.schedule** | EventBridge Scheduler | 스케줄 작업 관리 |
| **net.kotlinx.aws.ses** | SES | 이메일 발송 |
| **net.kotlinx.aws.sfn** | Step Functions | 워크플로우 실행 및 관리 |
| **net.kotlinx.aws.sqs** | SQS | 큐 메시지 발행/구독, Worker 패턴 |
| **net.kotlinx.aws.ssm** | Systems Manager | 파라미터 스토어 관리 |
| **net.kotlinx.aws.sts** | STS | 임시 자격 증명 발급 |
| **net.kotlinx.ai.mcp** | MCP 프로토콜 | Model Context Protocol 지원 |
| **net.kotlinx.api.ecos** | 한국은행 ECOS | 경제통계 API 연동 |
| **net.kotlinx.domain.batchStep** | 배치 스텝 도메인 | Step Functions용 배치 처리 도메인 |
| **net.kotlinx.dooray** | 두레이 | 두레이 메신저 API 연동 |
| **net.kotlinx.email** | 이메일 | 이메일 파싱 및 처리 |
| **net.kotlinx.file** | 파일 확장 | S3 파일 처리 확장 |
| **net.kotlinx.github** | GitHub | GitHub API 연동, 저장소/이슈 관리 |
| **net.kotlinx.google** | Google API | Calendar, Drive, OTP, OAuth, Sheet, Vision 등 |
| **net.kotlinx.guava** | Guava | Google Guava 라이브러리 확장 |
| **net.kotlinx.jsoup** | Jsoup | HTML 파싱 및 스크래핑 |
| **net.kotlinx.kaml** | YAML | YAML 파싱 (kotlinx.serialization 기반) |
| **net.kotlinx.knotion** | 노션 블록 | 노션 블록 DSL 빌더 |
| **net.kotlinx.koin** | Koin DI | Koin 의존성 주입 확장 |
| **net.kotlinx.ktor** | Ktor | Ktor 클라이언트 확장 |
| **net.kotlinx.lazyLoad** | 지연 로딩 확장 | S3 등 원격 리소스 지연 로딩 |
| **net.kotlinx.lock** | 분산 락 | DynamoDB 기반 분산 락 구현 |
| **net.kotlinx.math** | 수학 | 수학 관련 유틸리티 |
| **net.kotlinx.notion** | Notion API | Notion 페이지/데이터베이스 CRUD |
| **net.kotlinx.okhttp** | OkHttp | HTTP 클라이언트 확장, 재시도 지원 |
| **net.kotlinx.openAi** | OpenAI API | ChatGPT 등 OpenAI 모델 호출 |
| **net.kotlinx.playwright** | Playwright | 브라우저 자동화 (헤드리스) |
| **net.kotlinx.reflect** | 리플렉션 | Kotlin 리플렉션 확장 |
| **net.kotlinx.slack** | Slack API | Slack 메시지 발송 및 워크플로우 |
| **net.kotlinx.string** | 문자열 확장 | Light 전용 문자열 유틸리티 |

### Heavy 프로젝트

> 웹서버 및 RDS를 위한 의존성 포함 (Spring Boot, Hibernate, JPA 등)

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **net.kotlinx.aws** | AWS 서비스 | Heavy용 AWS 클라이언트 확장 |
| **net.kotlinx.awscdk** | AWS CDK | CDK 스택 정의 및 배포 (CICD, ECS, Lambda, SFN 등) |
| **net.kotlinx.dataframe** | 데이터프레임 | Kotlin DataFrame 라이브러리 확장 |
| **net.kotlinx.domain** | 도메인 모델 | Heavy용 도메인 객체 |
| **net.kotlinx.dropbox** | Dropbox API | Dropbox 파일 업로드/다운로드 |
| **net.kotlinx.excel** | Excel 처리 | Apache POI 기반 엑셀 읽기/쓰기 |
| **net.kotlinx.gradle** | Gradle | Gradle 빌드 관련 유틸리티 |
| **net.kotlinx.hibernate** | Hibernate | JPA PostListener, PhysicalNamingStrategy |
| **net.kotlinx.jdbc** | JDBC | JDBC 연결 및 쿼리 실행 헬퍼 |
| **net.kotlinx.jpa** | JPA | Entity 탐색, 컬럼/테이블 정보 추출 |
| **net.kotlinx.kqdsl** | Kotlin QueryDSL | QueryDSL Kotlin 확장 (파라미터, Path 처리) |
| **net.kotlinx.passay** | 비밀번호 검증 | Passay 라이브러리 기반 비밀번호 정책 |
| **net.kotlinx.sftp** | SFTP | SFTP 파일 전송 |
| **net.kotlinx.spring** | Spring Framework | Spring Batch, MVC, Security, WebFlux 확장 |
| **net.kotlinx.validation** | 검증 확장 | Heavy용 검증 로직 |