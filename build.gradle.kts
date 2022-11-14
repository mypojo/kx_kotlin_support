plugins {
    kotlin("jvm") version "1.7.10"
    application
}

java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
    group = "net.kotlinx.kotlin_support"
    version = "1.22.1114"

    repositories {
        mavenCentral()
    }
}

subprojects {

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }
}

/** 의존관계거 없는 바닐라 코틀린 */
project(":core1") {

}

/**
 * 최소한의 기본 코틀린 패키지 +@  사용
 * 람다 실행의 마지노선??
 *  */
project(":core2") {
    dependencies {
        //==================================================== 내부 의존성 ======================================================
        implementation(project(":core1"))

        //==================================================== 로깅 ======================================================
        implementation("io.github.microutils:kotlin-logging-jvm:2.0.10")
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")
        implementation("org.codehaus.janino:janino:3.1.0") //파일롤링 표현식 필터처리에 필요함

    }
}
