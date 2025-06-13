//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")

    //spring-hibernate 플러그인
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

//==================================================== 프로젝트별 설정 ======================================================

apply(plugin = "io.spring.dependency-management")

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable") //BasicMetadata 등에 기본 생성자 강제생성
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("org.springframework.stereotype.Service")
}

dependencies {

    /** 부트전용 의존성 적용 (버전 명시 필요없어짐) */
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))  //https://youtrack.jetbrains.com/issue/KT-53426

    //==================================================== 내부 의존성 ======================================================
    api(project(":heavy"))

    //==================================================== 스프링 부트 시리즈 (버전x) ======================================================
    //이하 api 로 정의시 하위 프로젝트에서 문제 발생함!.
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-core")
    implementation("org.springframework.retry:spring-retry") //이건 사용 안할수도 있음

    //==================================================== 벨리데이션 ======================================================
    implementation("org.hibernate.validator:hibernate-validator") //버전표기 X. 약 1mb

    //==================================================== JWT 관련 (runtimeOnly로 선언된 의존성도 런타임에는 자동으로 전파됨) ======================================================
    api("io.jsonwebtoken:jjwt-api:_")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:_")
    runtimeOnly("io.jsonwebtoken:jjwt-gson:_")  // jackson -> gson 으로 변경

    //==================================================== 배치 관련 ======================================================
    implementation("com.opencsv:opencsv:_")//java 버전의 CSV 유틸. 하위호환성을 위해서 일단 남겨놨다. kotlin용을 대신해서 쓸것!

    implementation("jakarta.batch:jakarta.batch-api:2.1.1") //??


}