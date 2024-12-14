
import net.kotlinx.gradle.get
import net.kotlinx.number.halfUp
import net.kotlinx.number.toSiText
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime

plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    `maven-publish` //메이븐 플러그인 배포
}

//==================================================== 공통 ======================================================

allprojects {

    println("[$name] buildDir = ${layout.buildDirectory.get()}")

    //모든 구성에서 특정 의존성을 제거함
    configurations.all {
        exclude(group = "commons-logging", module = "commons-logging") //로그백하고 충돌남
        exclude(group = "org.slf4j", module = "slf4j-simple") //Class path contains multiple SLF4J providers 회피
    }

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    //두 버전 일치시켜야함
    java {
        toolchain { languageVersion = JavaLanguageVersion.of(21) }
        withSourcesJar() //소스코드 포함해서 배포
        //withJavadocJar() //메이븐 센트럴 빌드에 필요 (지금 안씀)
    }
    kotlin { jvmToolchain(21) }

    //자바 xx로 타게팅
    tasks.compileKotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict") //null값 체크 strict
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    group = providers["group"]
    version = providers["version"]

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    tasks.test {
        useJUnitPlatform {
            //필터 설정은 여기서 하지 않음 ( 매칭이 없으면 No tests found for Given include 오류)
            filter {
                includeEngines = setOf("kotest")  // kotest만 실행
            }

            //kotest 설정 (JUnit 아님!)
            maxHeapSize = "4096m"  //gradle test 의 경우 JVM 옵션이 아니라 여기 설정해야함.. (왜인지 모르겠음)
        }
        testLogging {
            showStandardStreams = true //혹시 테스트가 실행될 수 있어서 로그 활성화
            //events("passed", "skipped", "failed")
        }
    }

    //==================================================== 공통 의존성 ======================================================
    dependencies {
        implementation(kotlin("stdlib"))

        //==================================================== 테스트 ======================================================
        testImplementation("io.kotest:kotest-runner-junit5:_") //kotest 의존성 추가.  현재버전의경우 scan 이슈가 있어서 프로퍼티 설정 해줘야함
        //testImplementation("io.kotest.extensions:kotest-extensions-koin:_") //kotest koin 확장 (일단 사용중지)
        testImplementation("io.mockk:mockk:_") //코틀린 모킹

        /**
         * 공통 DI를 사용할 수 있게 해줌 -> notebook에서 사용하려면 test 폴더가 아닌 main에 있어야함
         * springboot 처럼 컴포넌트 스캔해서 하는 애들은 이렇게 다 모아버리면 컴포넌트들이 충돌나서 안된다!  주의!!
         * */
        testImplementation(project(":work"))
    }

    /**
     * 모든 의존성 크기 출력 (람다 레이어 / docker 용량 확인 등)
     * https://docs.aws.amazon.com/ko_kr/lambda/latest/dg/packaging-layers.html
     * buildSrc로 옮기면 gradle 확장이 없어서 에러남
     * */
    tasks.create("allDependencies", Zip::class) {
        group = "build"
        description = "AWS 람다 레이어용 전체 의존성 압축파일 생성"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        outputs.upToDateWhen { false } //더티체크 하지않고 무조건 실행

        val files = configurations.runtimeClasspath.get()
        from(files) {
            into("java/lib") //java 디렉토리 안에 연관 의존성 저장
        }
        archiveFileName = "allDependencies.zip"
        doLast {
            val sumOf = files.sumOf { it.length() }
            println("용량확인.. $archiveFile ->  ${sumOf.toSiText()}")
            listOf("이름", "용량", "비율").toTextGridPrint {
                files.sortedByDescending { it.length() }.take(100).map {
                    arrayOf(it.name, it.length().toSiText(), "${(it.length() * 100.0 / sumOf).toBigDecimal().halfUp(2)}%")
                }
            }
        }
    }

}

/**
 * public 배포
 * jitpack.io 으로 해도 되는데 직접 배포하는게 더 좋은듯
 * */
publishing {
    publications {
        fun pub(projectName: String) {
            create<MavenPublication>("maven-${projectName}") {
                groupId = "net.kotlinx.kotlin_support"
                artifactId = projectName
                from(project(":${projectName}").components["java"])
            }
        }
        //모든 의존성이 순서대로 다 있어야함. 병렬처리 해보려다 접음.
        pub("core")
        pub("light")
        pub("heavy").run {
            pub("heavy_boot3")
        }
    }
    repositories {

        maven {
            //repsy 서버가 불안정해서 서브로 github을 추가했다.
            // -> github의 패키지 서비스는 퍼블릭 리파지토리도 토큰을 넣어야 하고, 매우 느려서 서브로만 사용
            val host = "repsy" to "https://repo.repsy.io/mvn/mypojo/kotlin_support"
            //val host = "github" to "https://maven.pkg.github.com/mypojo/kx_kotlin_support"
            url = uri(host.second)
            credentials {
                username = providers["${host.first}.maven.username"]
                password = providers["${host.first}.maven.password"]
            }
        }
    }
}

/**
 * org.gradle.configuration-cache=true 가 있어야 doLast가 병렬 실행됨
 * 일반적인 빌드는 --parallel 만 있어도 병렬 처리됨
 * */
tasks.register("deployAll") {
    group = "aws"
    dependsOn(":t1", ":t2", ":t3")
    doLast {
        println("배포 3종 완료!")
    }
}

tasks.register("t1") {
    group = "aws"
    doLast {
        println("작업1!! ${LocalDateTime.now().toKr01()}")
        Thread.sleep(1000 * 3)
        println("작업1!! ${LocalDateTime.now().toKr01()}")
    }
}

tasks.register("t2") {
    group = "aws"
    doLast {
        println("작업2!! ${LocalDateTime.now().toKr01()}")
        Thread.sleep(1000 * 3)
        println("작업2!! ${LocalDateTime.now().toKr01()}")
    }
}

tasks.register("t3") {
    group = "aws"
    doLast {
        println("작업3!! ${LocalDateTime.now().toKr01()}")
        Thread.sleep(1000 * 3)
        println("작업3!! ${LocalDateTime.now().toKr01()}")
    }
}
