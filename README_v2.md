# kx_kotlin_support

Kotlin JVM 기반 멀티모듈 유틸리티 라이브러리. AWS 서버리스 아키텍처와 엔터프라이즈 통합에 특화.

[![License](https://img.shields.io/badge/license-MIT-blue)](https://opensource.org/license/mit-0/)

## 주요 특징

- **Core** - 최소 의존성 유틸리티 (Collection, Time, CSV, JSON, Validation 등)
- **AWS 서비스 통합** - S3, DynamoDB, Lambda, Kinesis, Bedrock, Step Functions 등
- **AWS CDK** - 전체 인프라를 Kotlin DSL로 정의 (VPC, ECS, CICD, SFN 등)
- **외부 API 연동** - Google, Slack, Notion, OpenAI, Dooray 등
- **Spring Boot & JPA** - Spring Batch, Hibernate, kotlin-jdsl 통합

## 모듈 구조

```
core  →  light  →  heavy  →  work
```

| 모듈 | 역할 | 주요 의존성 |
|------|------|------------|
| **core** | 최소 의존성 유틸리티 | Kotlin stdlib, coroutines, serialization, Gson, logback |
| **light** | AWS + 외부 서비스 (Lambda용) | AWS SDK Kotlin, Koin, OkHttp, Ktor, Slack, OpenAI |
| **heavy** | Spring Boot + DB + CDK (서버용) | Spring Boot, JPA, Hibernate, AWS CDK, POI |
| **work** | 테스트 & 실험 | Kotest 모듈, Deeplearning4j, KOMORAN, Delta Sharing |

## 빠른 시작

### Gradle 설정

```kotlin
repositories {
    maven { url = uri("https://repo.repsy.io/mvn/mypojo/kotlin_support") }
}

dependencies {
    // 필요한 모듈만 선택
    implementation("net.kotlinx.kotlin_support:core:2025-07-21")
    implementation("net.kotlinx.kotlin_support:light:2025-07-21")
    implementation("net.kotlinx.kotlin_support:heavy:2025-07-21")
}
```

### 요구사항
- JDK 21+
- Kotlin 2.2.0+
- Gradle (래퍼 포함)

---

## 코드 예제

### AWS Kinesis 실시간 대량 처리 - 요청

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

### AWS Kinesis 실시간 대량 처리 - 워커

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

### AWS CDK - CICD (GitHub & CodePipeline)

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
    concurrentBuildLimit = 1
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
        DeploymentType.PROD -> listOf(EventSets.CodekPipeline.FAILED)
        DeploymentType.DEV -> listOf(EventSets.CodekPipeline.FAILED, EventSets.CodekPipeline.SUCCESSED)
    }
    byGithub(MyProject.GITHUB_ROOT, MyProject.PROJECT_DMP, "arn:aws:codeconnections:ap-northeast-2:xxxx")
    create(stack)
}
```

### AWS CDK - ECS 블루/그린 배포

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
        .healthyThresholdCount(2)
        .unhealthyThresholdCount(2)
        .path("/api/healthcheck")
        .build()

    when (CdkInterface.DEPLOYMENT_TYPE) {
        DeploymentType.PROD -> createServiceBlueGreen(stack)
        DeploymentType.DEV -> createServiceRolling(stack)
    }
    cdkLogGroup.addLogAnomalyDetector(stack)
}

val hostedZone = HostedZoneUtil.load(stack, "xxx.com")
val domain = MyEcs.DOMAINS[CdkInterface.DEPLOYMENT_TYPE]!!
Route53Util.arecord(stack, hostedZone, domain, web.alb.toRecordTarget())
```

### AWS CDK - Step Functions 대량 데이터 분할 처리

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

## 패키지 상세

### Core

> 최소한의 의존성을 가진 핵심 유틸리티 (약 212 파일)

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **ai** | AI 텍스트 모델 추상화 | AiTextClient 인터페이스, 모델 입출력 구조체 |
| **calculator** | 배치 청크 계산 | 대용량 배치 작업의 청크 분할 및 진행률 추적 |
| **collection** | 컬렉션 확장 함수 | List/Map/Set 확장 (diff, flattenAny, groupBy 등), RoundRobin, RangeMap |
| **concurrent** | 동시성 처리 | CacheMap, ScopeChannel, MicroBatchQueue, ThreadSupport |
| **core** | 핵심 유틸리티 | @Kdsl DSL 마커, CoreUtil, DataConverter |
| **counter** | 이벤트 카운터 | EventTimeChecker, Latch |
| **csv** | CSV 파일 처리 | CSV 읽기/쓰기, 집계, Flow 기반 변환, 청크 도구 |
| **delegate** | 델리게이트 패턴 | Map 기반 속성 델리게이트 (MapAttribute) |
| **domain** | 공통 도메인 모델 | Menu 트리, Query 추상화 (QueryData/QueryModule), Tree 유틸 |
| **exception** | 예외 처리 | KnownException |
| **file** | 파일 처리 | 파일명 유틸, ZIP 압축/해제 |
| **html** | HTML 생성 | kotlinx-html 확장 DSL, HtmlTable, 스타일 |
| **id** | ID 생성기 | IdGenerator, Identity, MockGenerator |
| **io** | 입출력 추상화 | InputResource / OutputResource |
| **json** | JSON 처리 | Gson 래퍼, Koson DSL, kotlinx-serialization 지원 |
| **logback** | 로깅 | LoggerSupport 확장, TempLogger (Gradle 빌드용) |
| **number** | 숫자 확장 | Int/Long/Double/Decimal 확장, 숫자 단축 표현, 단위 변환 |
| **regex** | 정규식 | 정규식 유틸리티 |
| **retry** | 재시도 로직 | 백오프 지원 RetryTemplate |
| **string** | 문자열 확장 | 문자열 변환, 한글 처리, CharUtil |
| **system** | 시스템 유틸 | DeploymentType, ResourceHolder, SystemUtil |
| **time** | 시간/날짜 처리 | LocalDate/LocalTime 확장, TimeFormat, WeekOfMonth |
| **validation** | 검증 | Bean Validation 커스텀 어노테이션, LINE conditional 검증 |
| **xml** | XML 처리 | XML 데이터 파싱 |

### Light

> AWS Lambda 최적화 모듈 - AWS 서비스 및 외부 API 연동 (약 405 파일)

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **aws** | AWS 클라이언트 코어 | AwsClient/AwsConfig 중심, 클라이언트 캐싱 |
| **aws.athena** | Athena 쿼리 | 테이블 정의, 파티션 관리, CloudTrail 연동 |
| **aws.batch** | AWS Batch | 배치 작업 제출 및 관리 |
| **aws.bedrock** | Bedrock AI | Claude 등 모델 호출, 프롬프트 체이닝, Agent Runtime |
| **aws.codeCommit** | CodeCommit | Git 저장소 연동 |
| **aws.cognito** | Cognito | 사용자 풀 관리 |
| **aws.dynamo** | DynamoDB | Enhanced Client, 멀티인덱스, 속성 매핑 |
| **aws.ecs** | ECS | 컨테이너 서비스 관리 |
| **aws.eventBridge** | EventBridge | 이벤트 발행/구독, 스케줄링 |
| **aws.fargate** | Fargate | 서버리스 컨테이너 실행 |
| **aws.firehose** | Firehose | 실시간 로그 스트리밍, Iceberg 연동 |
| **aws.iam** | IAM | 자격증명 관리 |
| **aws.kinesis** | Kinesis | Task/Worker 패턴으로 실시간 대량 데이터 처리 |
| **aws.lambda** | Lambda | SnapStart 핸들러, 디스패처 (동기/비동기), S3Logic |
| **aws.lambdaUrl** | Lambda URL | Lambda 함수 URL 입출력 |
| **aws.logs** | CloudWatch Logs | 로그 조회 |
| **aws.rdsdata** | RDS Data API | Aurora Serverless v2 쿼리 |
| **aws.s3** | S3 | Flow 기반 업로드/다운로드, Presign, 멀티파트 |
| **aws.schedule** | Scheduler | EventBridge Scheduler, Cron 표현식 |
| **aws.ses** | SES | 이메일 발송 |
| **aws.sfn** | Step Functions | 워크플로우 실행 |
| **aws.sqs** | SQS | Worker/Task 패턴으로 큐 처리 |
| **aws.ssm** | Systems Manager | 파라미터 스토어 |
| **aws.sts** | STS | 임시 자격증명, AWS 계정 정보 |
| **ai** | AI 확장 | AiModelSet, MCP 서버 |
| **api** | 외부 API | 한국은행 ECOS, 한국수출입은행 |
| **domain.batchStep** | 배치 스텝 | Step Functions 연동 배치 단계 처리 |
| **domain.batchTask** | 배치 태스크 | 대용량 데이터 처리 프레임워크 |
| **domain.job** | Job 관리 | 이벤트, 트리거, 스케줄링 |
| **domain.item** | 아이템 관리 | DynamoDB 아이템, 에러 로그 |
| **dooray** | Dooray | 메신저 알림, 드라이브 파일 관리 |
| **email** | 이메일 수신 | Jakarta Mail 기반 이메일 파싱 |
| **github** | GitHub API | 커밋 조회 |
| **google** | Google API | Calendar, Sheets, OTP, reCAPTCHA |
| **guava** | Guava 확장 | ClassFinder, EventBus, Hash, TypeToken |
| **koin** | Koin DI | Koins 유틸 (koin/koinLazy/startupReset) |
| **ktor** | Ktor 서버 | JWT 인증, 라우팅, 세션 |
| **lock** | 분산 락 | DynamoDB 기반 ResourceLockManager |
| **math** | 수학 | 선형/다항 회귀분석 |
| **notion** | Notion API | 페이지/DB CRUD |
| **okhttp** | OkHttp | HTTP 클라이언트 확장 |
| **openAi** | OpenAI | ChatGPT 등 모델 호출 |
| **playwright** | Playwright | 브라우저 자동화, 스크린샷 |
| **retrofit** | Retrofit | REST 클라이언트 |
| **slack** | Slack | 메시지/알림 전송 |

### Heavy

> Spring Boot + DB + AWS CDK 풀스택 모듈 (약 263 파일)

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **aws.budgets** | 예산 관리 | AWS 월별 예산 알림 |
| **aws.cloudfront** | CloudFront | 캐시 무효화, 블루/그린 배포 |
| **aws.codeDeploy** | CodeDeploy | ECS 블루/그린 배포, AppSpec |
| **aws.cost** | 비용 분석 | Cost Explorer 데이터를 Excel로 출력 |
| **aws.ecr** | ECR | 컨테이너 레지스트리 관리 |
| **aws.glue** | Glue | 데이터베이스/크롤러 관리 |
| **aws.lakeformation** | Lake Formation | 태그 기반 접근 제어 |
| **aws.quicksight** | QuickSight | 대시보드, 데이터셋 관리 |
| **aws.rds** | RDS | IAM 인증 HikariCP DataSource |
| **awscdk.basic** | CDK 기본 | S3, LogGroup, Parameter, 태그 |
| **awscdk.channel** | CDK 채널 | EventBridge Schedule, SQS, SNS |
| **awscdk.cicd** | CDK CI/CD | CodePipeline, CodeBuild, CodeCommit |
| **awscdk.cloudwatch** | CDK 모니터링 | 알람, 대시보드 위젯 |
| **awscdk.cognito** | CDK Cognito | 사용자 풀/클라이언트 |
| **awscdk.data** | CDK 데이터 | DynamoDB, Athena, Firehose, Glue |
| **awscdk.ec2** | CDK EC2 | Bastion 호스트 |
| **awscdk.ecs** | CDK ECS | ECR, ECS Web, Batch Job Queue |
| **awscdk.iam** | CDK IAM | 역할/정책/사용자그룹, GuardDuty |
| **awscdk.lambda** | CDK Lambda | Lambda 함수, API Gateway, Lambda Layer |
| **awscdk.network** | CDK 네트워크 | VPC, Route53, CloudFront, WAF, ACM |
| **awscdk.sfn** | CDK Step Functions | SFN 체인, 분기, 병렬, 대기 |
| **dataframe** | 데이터프레임 | Kotlin DataFrame 확장 |
| **domain.jpa** | JPA 엔티티 | BasicMetadata, EntityWithId |
| **dropbox** | Dropbox | 파일 업로드/다운로드 |
| **excel** | Excel | Apache POI 기반 읽기/쓰기/스타일링 |
| **gradle** | Gradle 빌드 | GradleBuilder (S3 동기화 등) |
| **hibernate** | Hibernate | underscore 네이밍, JPA 리스너 |
| **jdbc** | JDBC | DataSource 설정, kotlin-jdbc 래퍼 |
| **kqdsl** | Query DSL | kotlin-jdsl JPQL 확장 |
| **sftp** | SFTP | JSch 기반 파일 전송 |
| **spring** | Spring 코어 | SpringBootContext (Lambda 지연 로딩), SpringHolder |
| **spring.batch** | Spring Batch | CSV Writer, DynamoDB Reader |

### Work

> 테스트, 실험, 통합 테스트 모듈 (약 46 파일)

| 패키지 | 주요 기능 | 설명 |
|--------|-----------|------|
| **kotest** | Kotest 베이스 | BeSpecLog/BeSpecLight/BeSpecHeavy 베이스 클래스 |
| **kotest.modules** | DI 모듈 | Koin 기반 통합 테스트 모듈 |
| **kotest.modules.ktor** | Ktor 테스트 | Ktor 서버 테스트 설정 |
| **kotest.modules.lambdaDispatcher** | Lambda 테스트 | Lambda 디스패처 리스너 모듈 |
| **komoran** | 형태소 분석 | KOMORAN 래퍼 |
| **delta.sharing** | Delta Lake | Delta Sharing 프로토콜 클라이언트 |

---

## 아키텍처

### Lambda 디스패처 패턴

```
API Gateway / EventBridge / S3 Event
        ↓
  AbstractRequestHandler (SnapStart)
        ↓
  LambdaDispatcher (이벤트 타입별 라우팅)
        ↓
  ┌─ SyncHandler (API 응답)
  └─ AsyncHandler (EventBridge/S3/SNS)
```

### DynamoDB 멀티인덱스 패턴

```
하나의 DynamoDB 테이블
  ├─ pk="JOB#001"  sk="meta"     → Job 엔티티
  ├─ pk="JOB#001"  sk="log#01"   → Job 로그
  ├─ pk="BATCH#A"  sk="step#1"   → BatchStep
  └─ pk="LOCK#X"   sk="owner"    → 분산 락
```

### 배치 프레임워크

```
EventBridge Schedule → Lambda (BatchTask)
        ↓
  Step Functions (BatchStep)
    ├─ Map 모드: 인라인 분할 처리
    └─ List 모드: 반복 조회 + 대기
        ↓
  DynamoDB (상태 관리) + S3 (데이터)
```

### CDK 인프라 구조

```
CdkProject
  ├─ Network Stack (VPC, Route53, ACM, WAF)
  ├─ Data Stack (DynamoDB, S3, Athena, Glue)
  ├─ Compute Stack (Lambda, ECS, Batch)
  ├─ CICD Stack (CodePipeline, CodeBuild)
  └─ Monitoring Stack (CloudWatch, Alarms)
```

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **언어** | Kotlin 2.2.0 / JDK 21 |
| **빌드** | Gradle (refreshVersions 플러그인) |
| **DI** | Koin (보조), Spring (프로덕션) |
| **테스트** | Kotest (BehaviorSpec) + MockK |
| **AWS SDK** | AWS SDK for Kotlin 1.5.x |
| **웹 프레임워크** | Ktor (경량), Spring Boot 3.4.x (풀스택) |
| **DB** | PostgreSQL + HikariCP + Hibernate + kotlin-jdsl |
| **인프라** | AWS CDK (Kotlin DSL) |
| **로깅** | kotlin-logging + Logback |
| **JSON** | Gson, Koson DSL, kotlinx-serialization |
| **AI** | AWS Bedrock, OpenAI, MCP SDK |

## 라이선스

MIT License
