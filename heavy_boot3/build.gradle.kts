//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

//==================================================== 프로젝트별 설정 ======================================================

apply(plugin = "io.spring.dependency-management")

dependencies {

    /** 부트전용 의존성 적용 (버전 명시 필요없어짐) */
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))  //https://youtrack.jetbrains.com/issue/KT-53426

    //==================================================== 내부 의존성 ======================================================
    api(project(":heavy"))

    //==================================================== 스프링 부트 시리즈 (버전x) ======================================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-core")
    implementation("org.springframework.retry:spring-retry") //이건 사용 안할수도 있음

    //==================================================== 벨리데이션 ======================================================
    implementation("org.hibernate.validator:hibernate-validator") //버전표기 X

    //==================================================== JWT 관련 ======================================================
    implementation("io.jsonwebtoken:jjwt-api:_")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:_")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:_")

    //==================================================== 배치 관련 ======================================================
    implementation("com.opencsv:opencsv:_") //의존성 문제 보고됨




}