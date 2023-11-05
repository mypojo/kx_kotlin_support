plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    `maven-publish` //메이븐 플러그인 배포
    //id("de.fayard.refreshVersions")
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

allprojects {

    println("[$name] buildDir = $buildDir")

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    //자바 11로 타게팅
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = providers["jvmTarget"]
        }
    }

    group = providers["group"]
    version = providers["version"]

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        filter {
            useJUnitPlatform {
                includeTags(providers["testIncludeTags"]) //이 태그가 있어야 빌드시 테스트 실행
            }
        }
        testLogging {
            showStandardStreams = true //혹시 테스트가 실행될 수 있어서 로그 활성화
        }
    }

    java {
        withSourcesJar() //소스코드 포함해서 배포
        //withJavadocJar() //메이븐 센트럴 빌드에 필요
    }

    //==================================================== 공통 의존성 ======================================================
    dependencies {
        implementation(kotlin("stdlib"))

        testApi(project(":heavy_test")) //공통 DI를 사용할 수 있게 해줌 -> notebook에서 사용하려면 test 폴더가 아닌 main에 있어야함

//        //==================================================== 테스트 ======================================================
//        testImplementation("org.junit-pioneer:junit-pioneer:1.9.1")  //환경변수 테스트용 (실서버 job 실행 등)
//        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1") //최신버전
//        testImplementation("io.mockk:mockk:1.13.8") //코틀린 모킹

        //testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:0.6.10")  //네이버 픽스쳐몽키.. 애매함 -> .jqwik-database 이파일 자꾸 생겨서 제거

        //코테스트 문제가 많아서 공통에서 제거함 -> 이유없이 메모리아웃 오류남
//        testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
//        testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    }

    /**
     * 모든 의존성만 (용향 확인 등)
     * https://docs.aws.amazon.com/ko_kr/lambda/latest/dg/packaging-layers.html
     * */
    tasks.create("allDependencies", Zip::class) {
        group = "build"
        description = "AWS 람다 레이어용 전체 의존성 압축파일 생성"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(configurations.runtimeClasspath.get()) {
            into("java/lib") //java 디렉토리 안에 연관 의존성 저장
        }
        archiveFileName = "allDependencies.zip"
        doFirst {
            println("용량확인..")
            configurations.runtimeClasspath.get().sortedByDescending { it.length() }.take(20).forEach {
                println(" -> ${it.length() / 1024 / 1024}  ${it.name}")
            }
        }
    }

}

//==================================================== 배포 ======================================================

//메이븐 센트럴 배포
//https://jitpack.io/#mypojo/kx_kotlin_support/-SNAPSHOT  이걸로 해도 됨 (직접 배포하는게 더 좋은듯)
//publishing {
//    publications {
//        fun pub(projectName: String) {
//            create<MavenPublication>("mavenCentral-${projectName}") {
//                groupId = "net.kotlinx"
//                artifactId = projectName
//                from(project(":${projectName}").components["java"])
//                pom {
//                    name.set(projectName)
//                    description.set("Kotlin Support Library")
//                    url.set("https://github.com/mypojo/kx_kotlin_support")
//                    licenses {
//
//                    }
//                }
//            }
//        }
//        //모든 의존성이 순서대로 다 있어야함
//        pub("core1")
//    }
//    repositories {
//        maven {
//            url = uri("s01.oss.sonatype.org")
//            credentials {
//                username = providers["jatbrains.space.maven.username"]
//                password = providers["jatbrains.space.maven.password"]
//            }
//        }
//    }
//}

/** private 배포 */
publishing {
    publications {
        fun pub(projectName: String) {
            create<MavenPublication>("maven-${projectName}") {
                groupId = "net.kotlinx.kotlin_support"
                artifactId = projectName
                from(project(":${projectName}").components["java"])
            }
        }
        //모든 의존성이 순서대로 다 있어야함. 선언적 설정이라 병렬 처리는 안되는듯..
        pub("core")

        val pubConfig = System.getenv()["pubConfig"]
        if (pubConfig == null) {
            pub("light")
            pub("heavy").run {
                pub("heavy_boot3")
            }
        } else {
            println("pubConfig = $pubConfig")
            pubConfig.split(",").forEach { pub(it) }
        }

    }
    repositories {
        maven {
            url = uri("https://maven.pkg.jetbrains.space/november/p/ost/kotlin-support")
            credentials {
                username = providers["jatbrains.space.maven.username"]
                password = providers["jatbrains.space.maven.password"]
            }
        }
    }
}

