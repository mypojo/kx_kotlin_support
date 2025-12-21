//여기 한먼만 하면 별도 apply 필요없음
plugins {

    //그래들 컨벤션 플러그인으로 공통부분 분리 가능하지만, 여기에서는 공통 그래들 설정만으로 충분한거 같아서 수행하지 않음 (2025-08)

    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization")
    //id("com.google.devtools.ksp")  //ksp 적용시 버전 에러남. k2 컴파일러 때문인듯.. 당장 필요하지 않으니 안정화 되면 사용해보자
}

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    implementation(kotlin("stdlib"))

    //==================================================== 내부 의존성 ======================================================
    api(project(":core"))
    //ksp(project(":ksp")) //ksp 적용시 이 의존성을 컴파일러에 추가

    //==================================================== jakarta 표준 ======================================================
    api("com.sun.mail:jakarta.mail:_")  //jakarta.mail:jakarta.mail-api 의 구현체. 구현체가 sun/eclp무조건 지정해야함

    //==================================================== 코틀린 & 젯브레인 시리즈 ======================================================
    val kotlinVersion: String by project
    api("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}") // 리플렉션 약 3.1메가. 살짝 부담되긴 함.  코틀린 버전하고 같이 따라감
    api("io.insert-koin:koin-core:_") //kotlin DI 도구. 모듈 설정에는 이걸 적용하기로함.  실무에서는 spring 사용

    //==================================================== 기본 http 클라이언트 ======================================================
    api("com.squareup.okhttp3:okhttp:_") //https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("aws.smithy.kotlin:http-client-engine-okhttp-jvm:_") //http 설정에 필요  https://mvnrepository.com/artifact/aws.smithy.kotlin/http-client-engine-okhttp-jvm

    //==================================================== AWS 최소의존성 ======================================================
    //리프레시 버전은 지정된 몇개의 의존성만 공통 버전을 지원한다. 아쉽게도 AWS 는 아님..
    //https://github.com/Splitties/refreshVersions/tree/main/plugins/dependencies/src/main/kotlin/dependencies 참고
    //이때문에 리프레시 버전에 하나 참조해서 , 프로퍼티로 사용함
    val awsVersion: String by project
    api("aws.sdk.kotlin:s3:$awsVersion")
    api("aws.sdk.kotlin:dynamodb:$awsVersion")
    api("aws.sdk.kotlin:kinesis:$awsVersion")
    api("aws.sdk.kotlin:firehose:$awsVersion")
    api("aws.sdk.kotlin:sqs:$awsVersion")
    api("aws.sdk.kotlin:lambda:$awsVersion")
    api("aws.sdk.kotlin:sfn:$awsVersion")
    api("aws.sdk.kotlin:athena:$awsVersion")
    api("aws.sdk.kotlin:ssm:$awsVersion") //용량 큼
    api("aws.sdk.kotlin:batch:$awsVersion") //걸리는데가 많아서 추가
    api("aws.sdk.kotlin:ecs:$awsVersion") //ECS 서비스 업데이트 때문에 여기 추가
    api("aws.sdk.kotlin:cloudwatchlogs:$awsVersion") //로그 조회기능때문에 위로 이동
    api("aws.sdk.kotlin:sts:$awsVersion") //용량 얼마 안되서 옮김. AWS ID 조회 등
    api("aws.sdk.kotlin:eventbridge:$awsVersion") //2m 밖에 안함
    api("aws.sdk.kotlin:ses:$awsVersion") //2m 밖에 안함
    api("aws.sdk.kotlin:codecommit:$awsVersion") //3m.. 그래도 넣자
    api("aws.sdk.kotlin:scheduler:${awsVersion}")  //새로운 이벤트브릿지 스케쥴 0.8mb
    api("aws.sdk.kotlin:rdsdata:${awsVersion}")  // 오로라 서버리스 v2 API 호출  https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/kotlin/usecases/serverless_rds
    api("aws.sdk.kotlin:cognitoidentityprovider:$awsVersion") //코그니토  4 mbyt

    //==================================================== AWS 배드락 ======================================================
    api("aws.sdk.kotlin:bedrock:$awsVersion") //약 2mb  시리즈 전체 -> https://central.sonatype.com/search?q=g%3Aaws.sdk.kotlin+bedrock&smo=true
    api("aws.sdk.kotlin:bedrockruntime:$awsVersion") //배드록 런다임. 약 1m 실제 모델 호출에 사용
    api("aws.sdk.kotlin:bedrockagent:$awsVersion") //배드록 에이전트.
    api("aws.sdk.kotlin:bedrockagentruntime:$awsVersion") //배드록 에이전트 런타임

    //==================================================== AWS 람다 ======================================================
    api("com.amazonaws:aws-lambda-java-core:_") //람다 핸들러 (엔드포인트 수신기) 이거만 있으도 되긴함
    api("com.amazonaws:aws-lambda-java-events:_")  //핸들러에 매핑되는 이벤트 객체들 (안쓰면 필요없음)
    //compile("com.amazonaws:aws-lambda-java-runtime-interface-client:_") //람다 도커용 인터페이스. 실제 런타임에는 꼭 붙여야한다. (공식이미지에도 미포함임)
    api("io.github.crac:org-crac:_")  //스냅스타트 후크 (클래스 로딩용)

    //==================================================== AWS JAVA V2 client (레거시 호환) ======================================================
    val awsJavaV2Version: String by project
    implementation("software.amazon.awssdk:apache-client:$awsJavaV2Version") //기본 HTTP 클라이언트. okhttp 없음.. ㅠ https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/http-configuration-url.html
    implementation("software.amazon.awssdk:dynamodb:$awsJavaV2Version") //DDB 분산락 작용용
    implementation("software.amazon.awssdk:sts:$awsJavaV2Version") // 또는 최신 버전
    api("com.amazonaws:dynamodb-lock-client:_") //DDB 분산락 클라이언트 정발버전 (spring tx에서 같이 사용)


    //====================================================커먼즈 ======================================================
    api("org.apache.commons:commons-text:_") // javacript 등의 이스케이핑에 사용된다. kotlin 네이티브가 없네..

    //==================================================== 구아바 (이정도는 괜찮겠지) ======================================================
    api("com.google.guava:guava:_") //약 3mb

    //==================================================== 노션 SDK ======================================================
    //api("org.jraf:klibnotion:_") //약 1mb 흠.. 미덥지 못하다. ktor  2점대 잘 동작하나 3점대 작동안함.. 업데이트가 안되는중.. 일단 무시 (향후 삭제할것)
    //api("com.petersamokhin.notionsdk:notionsdk:_") //사용하니 ktor 1점대 쓰고있네.. 업데이트 안되는듯.. 일단 무시 (향후 삭제할것)

    //==================================================== 슬랙 ======================================================
    api("com.slack.api:slack-api-client:_") //기본 API만 포함함
    api("com.slack.api:slack-api-model-kotlin-extension:_") //코틀린 확장

    //==================================================== 구글 API ======================================================
    //사용하기 키 발급받아서 사용하기 너무 불편함!! 일단 사용처는 없음
    //implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") //https://github.com/googleapis/google-auth-library-java  구글인증은 이걸로 다 바뀐듯함 -> 나는 안씀
    implementation("com.google.gdata:core:_") //구글 기본세트


    //구글 기본세트는 메이븐에 없음 -> https://central.sonatype.com/artifact/com.google.apis/google-api-services-oauth2/v2-rev20200213-1.32.1
    //이때문에 일단 리프레시 버전 안쓰고 하드코딩함
    //implementation("com.google.api-client:google-api-client:2.6.0") //구글 Jib 빌드시 사용.
    //주의!! 구글은 API 2.x 를 내놨지만 시트 등은 여전히 1x 버전임. (com.google.api-client:google-api-client)
    // 호환안되고 확장해줄 의사도 앖음 -> 장기적으로는 rest 직접 호출이 바람직함
    //실무 사용시 충돌나는경우 전역설정에 force 로 해결하면됨
    implementation("com.google.apis:google-api-services-oauth2:v2-rev20200213-1.32.1") //구글 기본세트
    implementation("com.google.apis:google-api-services-calendar:v3-rev20250404-2.0.0") //캘린더
    implementation("com.google.apis:google-api-services-sheets:v4-rev20250616-2.0.0") //구글시트
    constraints {
        implementation("com.google.api-client:google-api-client:1.34.1") { because("api-client와 구글서비스 호환때문에 강제로 1.x 버전 사용") }
        implementation("com.google.http-client:google-http-client-gson:1.43.3") { because("api-client와 구글서비스 호환때문에 강제로 1.x 버전 사용") }
    }

    //==================================================== OPEN-AI ======================================================
    api("com.aallam.openai:openai-client:_") //open API (챗GPT) kotlin client.  커스텀 host를 로드하려면 implementation -> api 로 해야함

    //==================================================== ktor-client (OPEN-AI / 노션 등 에서 사용) ======================================================
    //클라이언트 버전은 서버 버전하고 동일하게 일단 가자. 충돌은 거기서 풀기
    implementation("io.ktor:ktor-client-core:_")
    implementation("io.ktor:ktor-client-okhttp-jvm:3.3.0") //open API 의 ktor core  베타인 3점대 쓰면 에러남../
    runtimeOnly("io.ktor:ktor-client-okhttp:_") //open API 의 ktor JVM http 엔진. 나는 okhttp 사용.

    //==== 이하 플러그인 ====
    implementation("io.ktor:ktor-client-auth:_")

    //==================================================== ktor-server (UI) ======================================================
    api("io.ktor:ktor-server-core-jvm:_")
    api("io.ktor:ktor-server-netty:_")
    api("io.ktor:ktor-server-test-host:_") // -> io.ktor:ktor-server-tests-jvm에서 이름이 변경된듯
//    api("io.ktor:ktor-server-tests-jvm:_") {
//        // 람다용 호출 때문에 testImplementation -> implementation 로 변경
//        exclude("io.ktor","ktor-client-apache")
//    }
    api("io.ktor:ktor-server-html-builder-jvm:_") //kotlin html 간단 확장

    //==== 이하 ktor 플러그인 ====

    // 인증 3종세트
    api("io.ktor:ktor-server-auth-jvm:_")
    api("io.ktor:ktor-server-auth-jwt-jvm:_")
    api("io.ktor:ktor-server-sessions-jvm:_")

    api("io.ktor:ktor-server-auto-head-response-jvm:_")
    api("io.ktor:ktor-server-host-common-jvm:_")
    api("io.ktor:ktor-server-status-pages-jvm:_")

    //==================================================== retrofit2 (rest API) ======================================================
    api("com.squareup.retrofit2:retrofit:_")
    api("com.squareup.retrofit2:converter-gson:_")
    api("com.squareup.okhttp3:logging-interceptor:_")

    //==================================================== 크롤링 ======================================================
    api("com.microsoft.playwright:playwright:_")

    //==================================================== AI ======================================================
    api("io.modelcontextprotocol:kotlin-sdk:_")  //공식 MCP SDK인데 코틀린버전은 매번 업데이트가 느려서 적용이 힘들다

    //==================================================== 기타 ======================================================
    api("gov.nist.math:jama:1.0.3") //https://mvnrepository.com/artifact/gov.nist.math/jama 회귀분석 패키지
    api("net.lingala.zip4j:zip4j:_") //zip 압축 & 암호설정 패키지. 200kb
    api("org.jsoup:jsoup:_") //크롤링  1.8.3
    api("com.charleskorn.kaml:kaml:_") //yaml 도구

}

/** 실행파일 + 모든 의존성. 이게 용량이 50mb 이하라면 다이렉트로 업로드 가능. */
tasks.register("fatJar", Jar::class) {
    group = "build"
    description = "AWS 람다 all-in-one 빌드용 (전체 의존이 50mb 이내여야함)"
    manifest.attributes["Main-Class"] = "com.example.MyMainClass" //AWS 람다 등록시 필요없음
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)  ///외부 의존성(dependencies)과 현재 프로젝트의 컴파일된 코드(tasks.jar)를 모두 포함
    from(tasks.jar.get().archiveFile.map(::zipTree))  //현재 프로젝트(light 모듈)의 컴파일된 코드를 JAR에 추가
    archiveFileName.set("fatJar.jar")
    isZip64 = true //archive contains more than 65535 entries
    doLast {
        // 람다 디플로이 및 버전 갱신...
    }
}