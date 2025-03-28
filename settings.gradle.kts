@file:Suppress("UnstableApiUsage")


pluginManagement {

    /** KSP 의 google 때문에 이 설정 추가함 */
    repositories {
        gradlePluginPortal() //기본 포탈
        google() //KSP 에 사용됨
    }

    plugins {
        //==================================================== 코어 ======================================================
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion //변수지정 안됨.. 아래 kotlinVersion하고 동일
        //kotlin("multiplatform") version kotlinVersion apply false // 아직은 이르다.. kotlin-jvm 하고 같이 사용 못함
        kotlin("plugin.serialization") version kotlinVersion  // apply 까지 해당 프로젝트에 해야지 컴파일할때 적용됨

        //==================================================== 스프링부트 ======================================================
        kotlin("plugin.spring") version kotlinVersion //같은값 입력

        val springBootVersion: String by settings
        id("org.springframework.boot") version springBootVersion //부트 기본 플러그인.
        val springBootDmVersion: String by settings
        id("io.spring.dependency-management") version springBootDmVersion  //세팅시 의존성 설정시 버전 명시 필요 없어짐.

        //==================================================== 컴파일러  ======================================================
        //https://kotlinlang.org/docs/all-open-plugin.html#command-line-compiler
        kotlin("kapt") version kotlinVersion  //어노테이션
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion  //스프링 플러그인 allOpen 등 지원
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion  // @Entity, @Embeddable, @MappedSuperclass 어노테이션이 붙은 모든 클래스에 자동으로 매개변수가 없는 생성자 추가

        //==================================================== KSP 플러그인 ======================================================
        //KSP 플러그인 (어노테이션 프로세서) => k2 컴파일러 사용시 옵션 입력해야함
        //코틀린 버전과 맞춰야 해서 리프레시 버전 사용하지 않음. -> kotlin 버전과 접미어를 맞춰줘야함.   https://github.com/google/ksp/releases
        //2025-02 k2 컴파일러 에러나서 일단 중단.. 향후 버전 2.1로 올려서 사용해보자.  https://github.com/google/ksp/blob/main/docs/ksp2.md
        val kotlinKspVersion: String by settings
        id("com.google.devtools.ksp") version kotlinKspVersion
    }

}

/**
 * 여기 설정해도 되고, build.grale 에 설정해도됨
 * */
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) //뭔지 잘 모름
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") } //가끔 사용함
        google() //com.google.devtools.ksp 때문에 사용
        maven { url = uri("https://maven.scijava.org/content/repositories/public/") } //형태소 분석기용 리파지토리
    }
}


//==================================================== refreshVersions 세팅 여러가지 ======================================================
plugins {
    id("com.gradle.enterprise") version "3.6.3"
////                        # available:"3.6.4"
////                        # available:"3.7"
////                        # available:"3.7.1"
////                        # available:"3.7.2"
////                        # available:"3.8"
////                        # available:"3.8.1"
////                        # available:"3.9"
////                        # available:"3.10"
////                        # available:"3.10.1"
////                        # available:"3.10.2"
////                        # available:"3.10.3"
////                        # available:"3.11"
////                        # available:"3.11.1"
////                        # available:"3.11.2"
////                        # available:"3.11.3"
////                        # available:"3.11.4"
////                        # available:"3.12"
////                        # available:"3.12.1"
////                        # available:"3.12.2"
////                        # available:"3.12.3"
////                        # available:"3.12.4"
////                        # available:"3.12.5"
////                        # available:"3.12.6"
////                        # available:"3.13"
////                        # available:"3.13.1"
////                        # available:"3.13.2"
////                        # available:"3.13.3"
////                        # available:"3.13.4"
////                        # available:"3.14"
////                        # available:"3.14.1"
////                        # available:"3.15"
////                        # available:"3.15.1"
////                        # available:"3.16"
////                        # available:"3.16.1"
////                        # available:"3.16.2"
////                        # available:"3.17"
////                        # available:"3.17.1"
////                        # available:"3.17.2"
////                        # available:"3.17.3"
////                        # available:"3.17.4"
////                        # available:"3.17.5"
////                        # available:"3.17.6"
////                        # available:"3.18"
////                        # available:"3.18.1"
////                        # available:"3.18.2"
////                        # available:"3.19"
////                        # available:"3.19.1"
////                        # available:"3.19.2"
    id("de.fayard.refreshVersions") version "0.60.2" //기타의존성 버전 자동갱신
////                            # available:"0.60.3"
////                            # available:"0.60.4"
////                            # available:"0.60.5"
}

/**
 * 리프레시 버전을 사용하는경우 커스텀 buildSrc 사용시 ignore 설정 및 약간의 테스트가 필요함
 * */
refreshVersions {
    enableBuildSrcLibs()
}

// https://dev.to/jmfayard/the-one-gradle-trick-that-supersedes-all-the-others-5bpg
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
        buildScanPublished {
            file("buildscan.log").appendText("${java.util.Date()} - $buildScanUri\n")
        }
    }
}

rootProject.name = "kx_kotlin_support"

include(
    "core", //최소 의존성으로 사용 가능

    "light", //람다용으로 의존성을 분리한것. GraalVM 활성화 전까지는 snapstart로 사용
    "heavy", //DB연결이 포함된 의존성
    "heavy_boot3", //스프링 부트3 의존성.

    "ksp", //어노테이션 프로세서 모음집
    "work", //로컬작업 및 테스트용
)
