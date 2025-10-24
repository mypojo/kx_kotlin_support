//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함

    //주의!! gradle 의 boot 의존성 API 가 하위까지 내려오지 않느거 같아서, boot로 따로 또 로드해줌
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

//==================================================== 프로젝트별 설정 ======================================================

dependencies {

    //==================================================== 내부 의존성 ======================================================
    api(project(":heavy"))

    //==================================================== AI - MCP ======================================================
    api("io.modelcontextprotocol:kotlin-sdk:_")

    //==================================================== 테스트용 도구 추가 ======================================================
    implementation("com.google.ortools:ortools-java:_") //구글 최적화도구 orTool https://developers.google.com/optimization/install/java/pkg_windows?hl=ko

    //==================================================== 테스트 api   ======================================================
    //api("io.mockk:mockk:_") //코틀린 모킹
    //api("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:0.6.10")  //네이버 픽스쳐몽키.. 애매함 -> .jqwik-database 이파일 자꾸 생겨서 제거

    //==================================================== 머신러닝 ======================================================
    api("org.deeplearning4j:deeplearning4j-core:_")  //https://deeplearning4j.konduit.ai/
    api("org.deeplearning4j:deeplearning4j-nlp:_") //NLP(Natural Language Processing, 자연어 처리)
    api("org.nd4j:nd4j-native-platform:_")  //ND4J는 다차원 행렬 계산을 실행하는 API를 제공

    api("gov.nist.math:jama:1.0.3") //https://mvnrepository.com/artifact/gov.nist.math/jama 회귀분석 패키지

    implementation("org.openpnp:opencv:4.5.1-2") //이미지 관련 처리

    //==================================================== 테스트 (하위에서 사용하기 위해서 API로 등록) ======================================================
    api("io.kotest:kotest-runner-junit5:_") //kotest 의존성 추가.  현재버전의경우 scan 이슈가 있어서 프로퍼티 설정 해줘야함
    api("io.kotest.extensions:kotest-extensions-koin:_") //kotest koin 확장


    //==================================================== 크롤링 ======================================================
    api("com.github.shin285:KOMORAN:3.3.9") //간이 형태소분석기   https://docs.komoran.kr/firststep/installation.html


    //==================================================== boot 재정의 ======================================================

    /** 부트전용 의존성 적용 (버전 명시 필요없어짐) */
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))  //https://youtrack.jetbrains.com/issue/KT-53426

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-batch")

    //runtimeOnly("io.ktor:ktor-client-okhttp:2.3.10") //open API 의 ktor JVM http 엔진. 나는 okhttp 사용. 메인 버전 안따라감!!

    //==================================================== UI 테스트도구 (스크린샷 등) ======================================================
    implementation("com.microsoft.playwright:playwright:1.41.0")

    //==================================================== AI Koog ======================================================
    implementation("ai.koog:koog-agents:_")

    //==================================================== 델타 세어 API (데이터브릭스 연동) ======================================================
    // 로깅 라이브러리 충돌 제거를 위한 헬퍼 함수
    fun ExternalModuleDependency.excludeLoggingConflicts() {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j2-impl")
    }

    //implementation("com.databricks.labs:delta-sharing-java-connector:0.1.0-SNAPSHOT")  -> 데이터브릭스가 제공하는 공식 래퍼인데 버전업 안해줘서 사용하지 않음
    val deltaVersion = "2.13:4.0.0"
    implementation("io.delta:delta-sharing-spark_${deltaVersion}") { excludeLoggingConflicts() } //스파크 베이스의 데이터 공유 표준 프로토콜 (대중적이지는 않은듯?)
    compileOnly("org.apache.spark:spark-sql_${deltaVersion}") { excludeLoggingConflicts() } //필수 지정해야함
    implementation("org.apache.spark:spark-core_${deltaVersion}") { excludeLoggingConflicts() }

    //로컬에서 Parquet 파일 읽기를 위한 의존성
    implementation("org.apache.parquet:parquet-avro:1.13.1") { excludeLoggingConflicts() }
    implementation("org.apache.parquet:parquet-hadoop:1.13.1") { excludeLoggingConflicts() }
    implementation("org.apache.hadoop:hadoop-common:3.3.6") { excludeLoggingConflicts() }

}