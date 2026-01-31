# AGENTS.md (CLAUDE.md)

## 프로젝트 개요

`kx_kotlin_support`는 Kotlin JVM 기반의 멀티모듈 유틸리티 라이브러리로, AWS 서버리스 아키텍처와 엔터프라이즈 통합에 특화되어 있다.
- 그룹: `net.kotlinx.kotlin_support`
- 버전 형식: 날짜 기반 (예: `2025-07-21`)
- 패키지 루트: `net.kotlinx`
- 배포: repsy.io Maven 저장소 (서브: GitHub Packages)

## 프로젝트 구조

```
kx_kotlin_support/
├── core/       # 최소 의존성 유틸리티 (JVM 표준 + Kotlin 기본 라이브러리만)
├── light/      # AWS SDK + 외부 서비스 통합 (Lambda/SnapStart 용)
├── heavy/      # Spring Boot + DB + AWS CDK (풀스택 서버용)
├── work/       # 로컬 테스트, 실험, 통합테스트 모듈
├── ksp/        # KSP 어노테이션 프로세서 (현재 비활성)
└── buildSrc/   # Gradle 빌드 유틸리티
```

### 의존성 체인

```
core → light → heavy → work
```

- **core**: 외부 의존성 최소. Kotlin stdlib, coroutines, kotlinx-serialization, Gson, logback, CSV, HTML DSL, Jakarta Validation
- **light**: core + AWS SDK Kotlin (S3, DynamoDB, Lambda, Kinesis, SQS, Bedrock 등), Koin DI, OkHttp, Ktor, Slack, OpenAI, Retrofit, Playwright, MCP SDK
- **heavy**: light + Spring Boot (Web, JPA, Batch, Security), AWS CDK, PostgreSQL, Hibernate, kotlin-jdsl, Exposed, Apache POI, Dropbox
- **work**: heavy + Kotest 통합모듈, Deeplearning4j, KOMORAN(형태소), Delta Sharing, OR-Tools, Koog Agents

## 빌드 & 실행

### 기본 설정
- **JDK**: 21 (toolchain)
- **JVM Target**: 21
- **Kotlin**: 2.2.0
- **Gradle**: 래퍼 사용, refreshVersions 플러그인으로 의존성 버전 관리 (`versions.properties`)
- **빌드 캐시**: `org.gradle.configuration-cache=true` 사용

### 주요 Gradle 태스크
```bash
# 테스트 실행 (kotest 엔진만 사용)
./gradlew test

# 배포 (light만)
./gradlew publish

# 배포 (전체: core + light + heavy)
pubType=all ./gradlew publish

# 의존성 용량 확인 (AWS Lambda 레이어용)
./gradlew :light:allDependencies

# Lambda용 fat JAR 빌드
./gradlew :light:fatJar

# 전체 청소
./gradlew clean
```

### IntelliJ 실행 구성 (.run/)
- `@ 리프레시 버전.run.xml` - refreshVersions 실행
- `@ 공개저장소 배포 (all).run.xml` - 전체 Maven 배포 (pubType=all)
- `# TEST [all].run.xml` - 전체 테스트
- `# TEST [light].run.xml` - light 모듈 테스트

## 테스트

### 프레임워크: Kotest
- BehaviorSpec 패턴 사용 (`Given-When-Then`)
- 모킹: MockK
- DI: Koin (테스트 모듈 기반)
- 태그 기반 필터링: `fast`, `slow`, `testing` (`kotest.properties`)
- 병렬 실행: 8 스레드 (`kotest.framework.parallelism = 8`)
- classpath scanning 비활성화 (성능 최적화)

### 테스트 구조
- 각 모듈의 `src/test/kotlin/`에 테스트 위치
- **work 모듈**이 통합 테스트의 DI 모듈 및 Kotest 베이스 클래스 제공
  - `BeSpecLog` - BehaviorSpec 기본 로깅 베이스 클래스
  - `BeSpecLight` / `BeSpecHeavy` - light/heavy 모듈용 테스트 베이스
- 모든 서브프로젝트의 `testImplementation`에 `project(":work")` 포함

### 테스트 작성 규칙
- test 폴더에 작성하되, 실제 AWS 리소스 호출 테스트는 태그로 분리
- `maxHeapSize = "4096m"` (gradle test 기준)

## 코딩 컨벤션

### 언어 & 스타일
- **언어**: Kotlin (strict null safety: `-Xjsr305=strict`)
- **주석**: 한국어 사용
- **패키지 네이밍**: `net.kotlinx.{도메인}` (예: `net.kotlinx.aws.dynamo`, `net.kotlinx.time`)
- **확장함수 파일 네이밍**: `{대상}Support.kt` (예: `LocalDateSupport.kt`, `MapSupport.kt`)
- **유틸 클래스 네이밍**: `{대상}Util.kt` (예: `FileNameUtil.kt`, `SystemUtil.kt`)
- **DSL 빌더 클래스 네이밍**: `{대상}Builder.kt` 또는 `{대상}Set.kt`

### DSL 패턴
- `@Kdsl` 커스텀 어노테이션 (`@DslMarker`)으로 1뎁스 DSL 제한
- 클래스에 `@Kdsl` 부착 (생성자 아닌 클래스에 직접)
- 빌더 패턴: `block: T.() -> Unit` 파라미터 사용

### DI 패턴
- **Koin**: 보조 DI로 사용. `Koins.koin<T>()` / `Koins.koinLazy<T>()`로 접근
- **Spring**: 프로덕션에서는 Spring DI 사용 (heavy 모듈)
- `Koins.startupOnlyOnce {}` - 테스트용 1회 초기화
- `Koins.startupReset {}` - CDK/Gradle 스크립트용 리셋 초기화

### AWS 클라이언트 패턴
- `AwsConfig` → `AwsClient` 중심. 클라이언트 캐싱 (`ConcurrentHashMap`)
- AWS SDK Kotlin 사용 (가능한 한). Java SDK v2는 레거시 호환용
- 확장함수로 AWS 서비스별 기능 구현 (예: `S3Support.kt`, `DynamoSupport.kt`)

### 로깅
- `kotlin-logging-jvm` (slf4j 래퍼) + `logback-classic`
- `TempLogger` - Gradle 빌드 스크립트용 임시 로거
- logback 설정: `core/src/main/resources/logback.xml`, `logback-default.xml`

## 모듈별 주요 패키지

### core (212 파일)
| 패키지 | 설명 |
|--------|------|
| `ai` | AI 모델 추상화 (AiTextClient 인터페이스) |
| `calculator` | 배치 청크 계산 |
| `collection` | Map/List/Set 확장함수, RoundRobin, RangeMap |
| `concurrent` | CacheMap, ScopeChannel, MicroBatchQueue, ThreadSupport |
| `core` | Kdsl 마커, CoreUtil, DataConverter |
| `counter` | EventTimeChecker, Latch |
| `csv` | CSV 읽기/쓰기, 집계, Flow 기반 처리 |
| `delegate` | Map 기반 프로퍼티 위임 |
| `domain.menu` | 메뉴 트리 구조 |
| `domain.query` | 쿼리 추상화 (QueryData, QueryModule) |
| `domain.tree` | 트리 유틸리티 |
| `exception` | KnownException |
| `file` | 파일명 유틸, ZIP 처리 |
| `html` | HTML DSL (kotlinx-html 확장) |
| `id` | ID 생성기 |
| `io` | InputResource/OutputResource 추상화 |
| `json` | Gson, Koson DSL, kotlinx-serialization 지원 |
| `logback` | 로거 확장, TempLogger |
| `number` | Int/Long/Double/Decimal 확장, 숫자 포맷팅, 단위 변환 |
| `string` | 문자열/한글 유틸 |
| `system` | DeploymentType, ResourceHolder, SystemUtil |
| `time` | LocalDate/LocalTime 확장, TimeFormat, WeekOfMonth |
| `validation` | Bean Validation (커스텀 어노테이션), 조건부 검증 (LINE conditional) |

### light (405 파일)
| 패키지 | 설명 |
|--------|------|
| `aws` | AWS 서비스 통합 코어 (AwsClient, AwsConfig) |
| `aws.athena` | Athena 쿼리, 테이블 관리, 파티션 |
| `aws.bedrock` | Bedrock AI (텍스트 변환, 프롬프트 체이닝) |
| `aws.dynamo` | DynamoDB Enhanced (멀티인덱스, 속성 매핑) |
| `aws.eventBridge` | EventBridge 스케줄링 |
| `aws.firehose` | Kinesis Firehose (로깅, Iceberg 연동) |
| `aws.kinesis` | Kinesis reader/writer/worker |
| `aws.lambda` | Lambda 핸들러 (SnapStart 지원), 디스패처, S3Logic |
| `aws.s3` | S3 Flow, Presign, 멀티파트 업로드 |
| `aws.sqs` | SQS worker/task |
| `aws.rdsdata` | Aurora Serverless v2 Data API |
| `ai` | AI 모델 세트, MCP 서버 |
| `api` | 외부 API (한국은행 ECOS, 한국수출입은행) |
| `domain.batchStep` | 배치 스텝 실행기 |
| `domain.batchTask` | 배치 태스크 프레임워크 (Step Functions 연동) |
| `domain.job` | Job 이벤트, 트리거, 스케줄링 |
| `domain.item` | DynamoDB 아이템 관리 |
| `dooray` | Dooray 메신저/드라이브 연동 |
| `email` | 이메일 수신 (Jakarta Mail) |
| `github` | GitHub API 커밋 조회 |
| `google` | Google Calendar, Sheets, OTP, reCAPTCHA |
| `guava` | ClassFinder, EventBus, Hash |
| `koin` | Koin DI 유틸 (Koins) |
| `ktor` | Ktor 서버 (JWT, 라우팅, 인증) |
| `lock` | DynamoDB 분산락 (ResourceLockManager) |
| `math` | 선형/다항 회귀분석 |
| `notion` | Notion API 연동 |
| `okhttp` | OkHttp 유틸 |
| `openAi` | OpenAI API 클라이언트 |
| `playwright` | 브라우저 자동화 (스크린샷) |
| `retrofit` | Retrofit REST 클라이언트 |
| `slack` | Slack 메시지/알림 |

### heavy (263 파일)
| 패키지 | 설명 |
|--------|------|
| `aws.budgets` | AWS 예산 알림 |
| `aws.cloudfront` | CloudFront 캐시 관리, 블루/그린 배포 |
| `aws.codeDeploy` | CodeDeploy (ECS 블루/그린) |
| `aws.cost` | Cost Explorer 비용 분석 |
| `aws.ecr` | ECR 레지스트리 |
| `aws.glue` | Glue 데이터베이스 |
| `aws.lakeformation` | Lake Formation 태그 관리 |
| `aws.quicksight` | QuickSight 대시보드 |
| `aws.rds` | RDS IAM 인증 DataSource (HikariCP) |
| `awscdk` | **AWS CDK IaC** (전체 인프라 정의) |
| `awscdk.basic` | S3, LogGroup, Parameter |
| `awscdk.channel` | EventBridge, SQS, SNS |
| `awscdk.cicd` | CodePipeline, CodeBuild, CodeCommit |
| `awscdk.cloudwatch` | 알람, 대시보드 |
| `awscdk.cognito` | Cognito 사용자 풀 |
| `awscdk.data` | DynamoDB, Athena, Firehose, Glue CDK |
| `awscdk.ec2` | Bastion 호스트 |
| `awscdk.ecs` | ECR, ECS Web, Batch, Compute Environment |
| `awscdk.iam` | IAM 역할/정책/사용자그룹 |
| `awscdk.lambda` | Lambda, API Gateway, Lambda Layer CDK |
| `awscdk.network` | VPC, Route53, CloudFront, WAF, ACM |
| `awscdk.sfn` | Step Functions CDK |
| `dataframe` | Kotlin DataFrame 확장 |
| `domain.jpa` | JPA 엔티티 (BasicMetadata, EntityWithId) |
| `dropbox` | Dropbox SDK |
| `excel` | Apache POI (Excel 생성/읽기) |
| `gradle` | Gradle 빌드 유틸 (GradleBuilder) |
| `hibernate` | Hibernate 네이밍 전략, JPA 리스너 |
| `jdbc` | JDBC 유틸, DataSource 설정, kotlin-jdbc |
| `kqdsl` | kotlin-jdsl JPQL 쿼리 DSL |
| `sftp` | SFTP 클라이언트 (JSch) |
| `spring` | Spring Boot 컨텍스트, 빈 관리 |
| `spring.batch` | Spring Batch (CSV Writer, DynamoDB Reader) |

### work (46 파일)
| 패키지 | 설명 |
|--------|------|
| `kotest` | Kotest 베이스 클래스, 태그 유틸 |
| `kotest.modules` | 통합 테스트용 Koin DI 모듈 |
| `kotest.modules.ktor` | Ktor 서버 테스트 설정 |
| `kotest.modules.lambdaDispatcher` | Lambda 디스패처 테스트 |
| `komoran` | KOMORAN 형태소 분석기 래퍼 |
| `delta.sharing` | Delta Lake 공유 프로토콜 클라이언트 |
| `slack` | Slack 메시지 전송 유틸 |
| `spring.jpa` | JPA 컬럼 크기 상수 |

## 핵심 아키텍처 패턴

### Lambda 디스패처 (light)
- `AbstractRequestHandler` → SnapStart 지원 Lambda 핸들러
- `LambdaDispatcher` → 이벤트 타입별 자동 라우팅
- `S3Logic` → S3 이벤트 기반 비즈니스 로직 실행

### DynamoDB 멀티인덱스 (light)
- `DbMultiIndex` → 하나의 테이블에 여러 엔티티 저장
- `DbTable` → Enhanced DynamoDB 클라이언트 래퍼
- `DbExpressionSet` → 쿼리/필터 표현식 빌더

### 배치 프레임워크 (light)
- `BatchStep` → Step Functions 연동 배치 단계
- `BatchTask` → 대용량 데이터 처리 태스크
- `Job` → 스케줄링된 작업 관리 (EventBridge 연동)

### Spring Boot 통합 (heavy)
- `SpringBootContext` → Lambda용 지연 로딩 Spring 컨텍스트
- `SpringHolder` → Spring 빈 접근 유틸
- Spring Batch → CSV/DynamoDB Reader/Writer

### CDK 인프라 (heavy)
- `CdkProject` → CDK 앱 구조화
- 전체 AWS 리소스를 Kotlin DSL로 정의 (VPC, Lambda, ECS, Step Functions 등)

## 주의사항

### 의존성 관리
- refreshVersions 플러그인 사용 → `versions.properties`에서 버전 관리
- AWS SDK 버전은 `gradle.properties`의 `awsVersion`으로 통일 (refreshVersions 미지원)
- `commons-logging` 글로벌 제외 (logback 충돌 방지)
- `slf4j-simple` 글로벌 제외 (다중 SLF4J 방지)

### 빌드 주의
- KSP 모듈은 현재 비활성 (`settings.gradle.kts`에서 주석 처리)
- K2 컴파일러 이슈로 KSP 사용 보류 (2025-02 기준)
- buildSrc에서 자체 라이브러리의 과거 안정 버전 사용 (`2025-04-01`)
- `allOpen` 플러그인으로 JPA 엔티티 클래스 open 처리 (heavy 모듈)

### 배포
- `pubType` 환경변수로 배포 범위 제어 (기본: light만, "all": heavy 포함)
- Maven 저장소: repsy.io (주), GitHub Packages (서브)
- 인증 정보: `gradle.properties`의 `repsy.maven.username/password`
