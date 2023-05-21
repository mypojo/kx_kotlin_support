//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================

apply(plugin = "io.spring.dependency-management")

dependencies {

    /** 부트전용 의존성 적용 (버전 명시 필요없어짐) */
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))  //https://youtrack.jetbrains.com/issue/KT-53426

    //==================================================== 내부 의존성 ======================================================
    api(project(":module1"))
    testApi(project(":core2").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== 스프링 부트 시리즈 (버전x) ======================================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.session:spring-session-core")

    //==================================================== JWT 관련 ======================================================
    val jwtVersion = "0.11.5"
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    //==================================================== 배치 관련 ======================================================
    implementation("com.opencsv:opencsv:5.7.1") //의존성 문제 보고됨

}