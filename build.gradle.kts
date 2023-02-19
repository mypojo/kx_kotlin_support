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

plugins {
    kotlin("jvm") version "1.7.20" //변수지정 안됨..
    java
    application
    `maven-publish` //메이븐 플러그인 배포
    //id("com.github.johnrengelman.shadow") version "7.1.2"  이런거 없어도 팻자르 잘 됨
}
java.sourceCompatibility = JavaVersion.VERSION_11

val awsVersion: String by extra("0.19.2-beta") //코틀린 버전 일단 사용
val kotlinVersion: String by extra("1.7.20")
val exposedVersion: String by extra("0.41.1")

allprojects {
    group = "net.kotlinx.kotlin_support"
    version = "2023-01-31"
    repositories {
        mavenCentral()
        //maven { setUrl("https://jitpack.io") }
    }

    //자바 11로 타게팅 (큰 의미 없음)
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}

subprojects {

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib"))

        //==================================================== 테스트 ======================================================
        testImplementation("org.junit-pioneer:junit-pioneer:1.9.1")  //환경변수 테스트용 (실서버 job 실행 등)
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1") //최신버전
        testImplementation("io.mockk:mockk:1.13.3") //코틀린 모킹
        testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
        testImplementation("io.kotest:kotest-assertions-core:5.5.4")

    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
        filter {
            useJUnitPlatform {
                includeTags("build") //이 태그가 있어야 빌드시 테스트 실행
            }
        }
    }

    java {
        withSourcesJar() //소스코드 포함해서 배포
    }

}

/** 아무것도 없음 */
project(":core1") {
    dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") //이미 AWS 때문에 코투틴이 로드 되어야함
    }
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

        //==================================================== javax ======================================================
        api("javax.validation:validation-api:2.0.1.Final")

        //==================================================== 로깅.. 맘에 안드네 ======================================================
        api("io.github.microutils:kotlin-logging-jvm:2.0.10") //slf4j의 래퍼. (로거 가져올때 사용)
        api("ch.qos.logback:logback-classic:1.4.5") //slf4j의 실제 구현체 (레벨 설정에 참조해야함)
        implementation("org.codehaus.janino:janino:3.1.9") //logback 파일롤링 표현식 필터처리에 필요함

        //==================================================== 코틀린 기본 ======================================================
        api("com.lectra:koson:1.2.4") // 코틀린 json DSL
        api("com.github.doyaaaaaken:kotlin-csv:1.6.0") //CSV.. 좀 신뢰가 안가는 이름이네. 87kb 로 매우 가벼움
        api("org.jetbrains.kotlinx:kotlinx-html:0.8.0")
        api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0") //간단 HTML 구성

        //==================================================== 기본옵션 ======================================================
        api("com.google.code.gson:gson:2.10") // 외부 의존성 없음.. 깔끔함.  300kb 이내
    }

}

/**
 * 람다 실행 최소패키지
 *  */
project(":aws1") {
    dependencies {
        //==================================================== 내부 의존성 ======================================================
        api(project(":core2"))

        //==================================================== 기본 http 클라이언트 ======================================================
        api("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
        implementation("aws.smithy.kotlin:http-client-engine-okhttp-jvm:0.14.2") //http 설정에 필요

        //==================================================== AWS ======================================================
        api("com.amazonaws:aws-lambda-java-core:1.2.2") //람다 핸들러 (엔드포인트 수신기) 이거만 있으도 되긴함
        api("com.amazonaws:aws-lambda-java-events:3.11.0")  //핸들러에 매핑되는 이벤트 객

        api("aws.sdk.kotlin:s3:${awsVersion}")
        api("aws.sdk.kotlin:dynamodb:${awsVersion}")
        api("aws.sdk.kotlin:kinesis:${awsVersion}")
        api("aws.sdk.kotlin:firehose:${awsVersion}")
        api("aws.sdk.kotlin:sqs:${awsVersion}")
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
project(":aws") {
    dependencies {
        //==================================================== 내부 의존성 ======================================================
        api(project(":core2"))
        api(project(":aws1"))

        //==================================================== 기본 (이정도는 괜찮겠지) ======================================================
        api("com.google.guava:guava:31.1-jre")

        //==================================================== AWS ======================================================
        api("aws.sdk.kotlin:athena:${awsVersion}")
        api("aws.sdk.kotlin:sts:${awsVersion}")
        api("aws.sdk.kotlin:lambda:${awsVersion}")
        api("aws.sdk.kotlin:iam:${awsVersion}")
        api("aws.sdk.kotlin:rds:${awsVersion}")
        api("aws.sdk.kotlin:ecs:${awsVersion}")  //ec2 생략
        api("aws.sdk.kotlin:ses:${awsVersion}")
        api("aws.sdk.kotlin:batch:${awsVersion}")
        api("aws.sdk.kotlin:ssm:${awsVersion}")
        api("aws.sdk.kotlin:eventbridge:${awsVersion}")
        api("aws.sdk.kotlin:sfn:${awsVersion}")
        api("aws.sdk.kotlin:codedeploy:${awsVersion}")
        api("aws.sdk.kotlin:secretsmanager:${awsVersion}")
    }
}


project(":module1") {
    dependencies {
        //==================================================== 내부 의존성 ======================================================
        api(project(":aws")) //API로 해야 하위 프로젝트에서 사용 가능하다.

        //==================================================== 코틀린 & 젯브레인 시리즈 ======================================================
        runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion") // 리플렉션 dto 변환용

        //젯브레인 ORM
        implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

        //==================================================== RDB ======================================================
        implementation("software.aws.rds:aws-mysql-jdbc:1.1.2") //aws 장애조치기능이 담긴 mysql 드라이버 & 모든 mysql과 호환가능. https://github.com/awslabs/aws-mysql-jdbc
        implementation("com.zaxxer:HikariCP:5.0.1")
    }
}

//==================================================== 배포 ======================================================
//https://jitpack.io/#mypojo/kx_kotlin_support/-SNAPSHOT  이걸로 해도 됨 (직접 배포하는게 더 좋은듯)
publishing {
    publications {
        fun pub(projectName: String) {
            create<MavenPublication>("maven-${projectName}") {
                groupId = "net.kotlinx.kotlin_support"
                artifactId = projectName
                from(project(":${projectName}").components["java"])
            }
        }
        //모든 의존성이 순서대로 다 있어야함
        pub("core1")
        pub("core2")
        pub("aws1")
        pub("aws")
        pub("module1")
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.jetbrains.space/november/p/ost/kotlin-support")
            credentials {
                username = project.properties["jatbrains.space.maven.username"].toString()
                password = project.properties["jatbrains.space.maven.password"].toString()
            }
        }
    }
}