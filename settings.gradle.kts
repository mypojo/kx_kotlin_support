@file:Suppress("UnstableApiUsage")

pluginManagement {
    plugins {
        //==================================================== 코어 ======================================================
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion //변수지정 안됨.. 아래 kotlinVersion하고 동일
        //kotlin("multiplatform") version kotlinVersion apply false // 아직은 이르다.. kotlin-jvm 하고 같이 사용 못함
        kotlin("plugin.serialization") version kotlinVersion  // apply 까지 해당 프로젝트에 해야지 컴파일할때 적용됨

        //==================================================== 스프링부트 ======================================================
        val springBootVersion: String by settings
        id("org.springframework.boot") version springBootVersion //부트 기본 플러그인.
        val springBootDmVersion: String by settings
        id("io.spring.dependency-management") version springBootDmVersion  //세팅시 의존성 설정시 버전 명시 필요 없어짐.
    }

}

//==================================================== refreshVersions 세팅 여러가지 ======================================================
plugins {
    id("com.gradle.enterprise") version "3.6.3"
    id("de.fayard.refreshVersions") version "0.60.2" //기타의존성 버전 자동갱신 -> 멀티프로젝트는 안되는듯?
}

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
    "work", //로컬작업 및 테스트용
)
