pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion //변수지정 안됨.. 아래 kotlinVersion하고 동일
        //kotlin("multiplatform") version "1.8.20" apply false //아직은 이르다..
        //==================================================== 커뮤니티 플러그인 ======================================================
        val jibVersion: String by settings
        id("com.google.cloud.tools.jib") version jibVersion //구글의 도커 이미지 빌드 도구. https://github.com/peter-evans/kotlin-jib
        //==================================================== 스프링부트 ======================================================
        val springBootVersion: String by settings
        id("org.springframework.boot") version springBootVersion //부트 기본 플러그인.
        val springBootDmVersion: String by settings
        id("io.spring.dependency-management") version springBootDmVersion  //세팅시 의존성 설정시 버전 명시 필요 없어짐.
    }
}

rootProject.name = "kx_kotlin_support"

include(
    "common",
    "core1",
    "core2",
    "aws1",
    "aws",
    "aws_cdk",
    "module1",
    "kopring",
)
