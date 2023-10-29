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

        //==================================================== 기타 ======================================================
        //id("de.fayard.refreshVersions") version "0.60.2" //기타의존성 버전 자동갱신 -> 멀티프로젝트는 안되는듯?
    }
}

rootProject.name = "kx_kotlin_support"

include(
//    "common",
    "core",
    "light",
    "aws_cdk",

    "light_v1",

    "heavy",
    "heavy_boot3",
    "heavy_notebook", //코틀린 주피터 노트북
    "heavy_test", //무거운 의존성 별도분리. 테스트용
)
