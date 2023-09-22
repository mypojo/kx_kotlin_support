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
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    implementation(kotlin("stdlib"))

    //==================================================== 내부 의존성 ======================================================
    api(project(":core2"))
    testApi(project(":core2").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== 기본 http 클라이언트 ======================================================
    api("com.squareup.okhttp3:okhttp:5.0.0-alpha.11") //https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("aws.smithy.kotlin:http-client-engine-okhttp-jvm:0.19.0") //http 설정에 필요  https://mvnrepository.com/artifact/aws.smithy.kotlin/http-client-engine-okhttp-jvm

    //==================================================== AWS ======================================================
    api("com.amazonaws:aws-lambda-java-core:1.2.2") //람다 핸들러 (엔드포인트 수신기) 이거만 있으도 되긴함
    api("com.amazonaws:aws-lambda-java-events:3.11.0")  //핸들러에 매핑되는 이벤트 객
    api("io.github.crac:org-crac:0.1.3")  //스냅스타트 후크 (클래스 로딩용)

    val awsVersion: String by project
    api("aws.sdk.kotlin:s3:$awsVersion")
    api("aws.sdk.kotlin:dynamodb:$awsVersion")
    api("aws.sdk.kotlin:kinesis:$awsVersion")
    api("aws.sdk.kotlin:firehose:$awsVersion")
    api("aws.sdk.kotlin:sqs:$awsVersion")
    api("aws.sdk.kotlin:lambda:$awsVersion")
    api("aws.sdk.kotlin:sfn:$awsVersion")
    api("aws.sdk.kotlin:athena:$awsVersion")
    api("aws.sdk.kotlin:ssm:$awsVersion")

    //====================================================커먼즈 ======================================================
    api("org.apache.commons:commons-text:1.10.0") // javacript 등의 이스케이핑에 사용된다. kotlin 네이티브가 없네..

    //==================================================== 슬랙 ======================================================
    api("com.slack.api:slack-api-client:1.29.1") //기본 API만 포함함

    //==================================================== 테스트 ======================================================
    testImplementation("org.junit-pioneer:junit-pioneer:1.9.1")  //환경변수 테스트용 (실서버 job 실행 등)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1") //최신버전
    testImplementation("io.mockk:mockk:1.13.3") //코틀린 모킹
    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}