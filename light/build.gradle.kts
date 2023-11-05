//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization")
}

apply {
    plugin("org.jetbrains.kotlin.plugin.serialization")
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 ex) providers["kotlinVersion"] */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    implementation(kotlin("stdlib"))

    //==================================================== 내부 의존성 ======================================================
    api(project(":core"))

    //==================================================== 코틀린 & 젯브레인 시리즈 ======================================================
    api("org.jetbrains.kotlin:kotlin-reflect:${providers["kotlinVersion"]}") // 리플렉션 약 3.1메가. 살짝 부담되긴 함.
    api("io.insert-koin:koin-core:_") //kotlin DI 도구. 모듈 설정에는 이걸 적용하기로함.  실무에서는 spring 사용

    //==================================================== 기본 http 클라이언트 ======================================================
    api("com.squareup.okhttp3:okhttp:_") //https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("aws.smithy.kotlin:http-client-engine-okhttp-jvm:_") //http 설정에 필요  https://mvnrepository.com/artifact/aws.smithy.kotlin/http-client-engine-okhttp-jvm

    //==================================================== AWS 최소의존성 ======================================================
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

    //==================================================== AWS 람다 ======================================================
    api("com.amazonaws:aws-lambda-java-core:_") //람다 핸들러 (엔드포인트 수신기) 이거만 있으도 되긴함
    api("com.amazonaws:aws-lambda-java-events:_")  //핸들러에 매핑되는 이벤트 객
    api("io.github.crac:org-crac:_")  //스냅스타트 후크 (클래스 로딩용)

    //====================================================커먼즈 ======================================================
    api("org.apache.commons:commons-text:_") // javacript 등의 이스케이핑에 사용된다. kotlin 네이티브가 없네..

    //==================================================== 구아바 (이정도는 괜찮겠지) ======================================================
    api("com.google.guava:guava:_") //약 3mb

    //==================================================== 슬랙 ======================================================
    api("com.slack.api:slack-api-client:_") //기본 API만 포함함

    //==================================================== 구글 API ======================================================
    //사용하기 키 발급받아서 사용하기 너무 불편함!! 일단 사용처는 없음
    //implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") //https://github.com/googleapis/google-auth-library-java  구글인증은 이걸로 다 바뀐듯함 -> 나는 안씀
    implementation("com.google.gdata:core:_") //구글 기본세트

    implementation("com.google.apis:google-api-services-oauth2:_") //구글 기본세트  v2-rev151-1.25.0
    implementation("com.google.apis:google-api-services-calendar:_") //캘린더
    implementation("com.google.apis:google-api-services-sheets:_") //구글시트

    //==================================================== AWS CDK (그냥 여기 둔다) ======================================================
    api("software.amazon.awscdk:aws-cdk-lib:_")   //https://mvnrepository.com/artifact/software.amazon.awscdk/aws-cdk-lib
    //api("software.constructs:constructs:10.1.278") //CDK 추가 빌딩블럭 -> 쓸만한게 없음
}

/** 실행파일 + 모든 의존성 */
tasks.create("fatJar", Jar::class) {
    group = "build"
    description = "AWS 람다 all-in-one 빌드용 (전체 의존이 50mb 이내여야함)"
    manifest.attributes["Main-Class"] = "com.example.MyMainClass" //AWS 람다 등록시 필요없음
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)
    with(tasks.jar.get())
    archiveFileName = "fatJar.jar"
    isZip64 = true //archive contains more than 65535 entries
    doLast {
        // 람다 디플로이 및 버전 갱신...
    }
}