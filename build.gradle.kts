import kotlinx.coroutines.runBlocking

buildscript {
    repositories { mavenCentral() }
    dependencies {
        //classpath("aws.smithy.kotlin:runtime-core:0.13.1") //람다 소스코드 배포용
        //classpath("aws.smithy.kotlin:http:0.13.1") //람다 소스코드 배포용
        //classpath("aws.smithy.kotlin:http-client-engine-okhttp:0.13.1") //람다 소스코드 배포용
        classpath("aws.sdk.kotlin:lambda:0.18.0-beta") //람다 소스코드 배포용
    }
}

//==================================================== 변수설정 (중간에 선언해야함) ======================================================
val awsVersion: String by extra("0.18.0-beta") //코틀린 버전 일단 사용
val kotlinVersion: String by extra("1.7.10")

plugins {
    kotlin("jvm") version "1.7.10" //안바뀌네.. ㅠㅠ
    java
    application
    //id("com.github.johnrengelman.shadow") version "7.1.2"  이런거 없어도 팻자르 잘 됨
}
java.sourceCompatibility = JavaVersion.VERSION_11




allprojects {
    group = "net.kotlinx.kotlin_support"
    version = "1.22.1114"
    repositories { mavenCentral() }
}

subprojects {

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib"))

        testImplementation("org.junit-pioneer:junit-pioneer:1.9.1")  //환경변수 테스트용 (실서버 job 실행 등)
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1") //최신버전
        testImplementation("io.mockk:mockk:1.13.3") //코틀린 모킹

    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }
}

project(":core1") {
    //아무것도 없음
}

/**
 * 최소한의 기본 코틀린 패키지 +@  사용
 * 람다 실행을 염두해 두고 설계함
 *  */
project(":core2") {

    dependencies {
        //==================================================== 내부 의존성 ======================================================
        api(project(":core1")) //API로 해야 하위 프로젝트에서 사용 가능하다.
        //testImplementation(project(":core1").sourceSets["test"].output) //코어 테스트에 있는 공통을 사용할 수 있게 해줌 => 일단 포기.. 빡친다.

        //==================================================== 로깅.. 맘에 안드네 ======================================================
        api("io.github.microutils:kotlin-logging-jvm:2.0.10") //slf4j의 래퍼. api로 선언해야 하위에서 사용 가능하다.
        implementation("ch.qos.logback:logback-classic:1.2.3") //slf4j의 실제 구현체
        //implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("org.codehaus.janino:janino:3.1.0") //logback 파일롤링 표현식 필터처리에 필요함
    }

}

project(":module1") {
    dependencies{
        //==================================================== 내부 의존성 ======================================================
        api(project(":core2")) //API로 해야 하위 프로젝트에서 사용 가능하다.

        runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}") // 리플렉션 dto 변환용
        implementation("com.github.doyaaaaaken:kotlin-csv:1.6.0") //CSV.. 좀 신뢰가 안가는 이름이네.
    }
}



/**
 * 람다 실행 최소패키지
 *  */
project(":aws_lambda1") {
    dependencies {
        //==================================================== 내부 의존성 ======================================================
        implementation(project(":core2"))

        //==================================================== AWS ======================================================
        implementation("com.amazonaws:aws-lambda-java-core:1.2.2") //람다 핸들러 (엔드포인트 수신기) 이거만 있으도 되긴함
        implementation("com.amazonaws:aws-lambda-java-events:3.11.0")  //핸들러에 매핑되는 이벤트 객

        implementation("aws.sdk.kotlin:s3:${awsVersion}")
        implementation("aws.sdk.kotlin:dynamodb:${awsVersion}")
        implementation("aws.sdk.kotlin:kinesis:${awsVersion}")
    }

    /**
     * 람다용 팻자르 빌드. 아래 실측 참고
     * https://www.notion.so/mypojo/Lambda-908049fe58b24e968779d8513cd26433
     *  */
    tasks.create("fatJar", Jar::class) {
        group = "build"
        description = "for aws lambda"
        manifest.attributes["Main-Class"] = "com.example.MyMainClass" //AWS 람다 등록시 필요없음
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
        from(dependencies)
        with(tasks.jar.get())
        doLast {

            runBlocking {
                val jarFile = File(project.buildDir, "libs/${archiveFileName.get()}")
                aws.sdk.kotlin.services.lambda.LambdaClient {
                    region = "ap-northeast-2"
                    credentialsProvider = aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider(profileName = "wabiz")
                }.use { client ->
                    client.updateFunctionCode(aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeRequest {
                        functionName = "firehose_kr"
                        zipFile = jarFile.readBytes()
                    })
                }
            }

        }

    }


}

/**
 * 람다 실행 최소패키지
 *  */
project(":aws_all") {
    dependencies {
        //==================================================== 내부 의존성 ======================================================
        implementation(project(":core2"))

        //==================================================== AWS ======================================================
        implementation("com.amazonaws:aws-lambda-java-core:1.2.2") //람다 엔드포인트 수신기. 이거만 있으도 됨

        implementation("aws.sdk.kotlin:sts:${awsVersion}")
        implementation("aws.sdk.kotlin:s3:${awsVersion}")
        implementation("aws.sdk.kotlin:dynamodb:${awsVersion}")
        implementation("aws.sdk.kotlin:lambda:${awsVersion}")
    }


}