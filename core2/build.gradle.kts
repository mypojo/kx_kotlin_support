//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization") version "1.8.21"
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================


tasks.getByName<Test>("test") {
    //코테스트 샘플 실행 제거
    exclude("**/string/**") //코테스트 제외
    exclude("**/concurrent/**") //코테스트 제외
}

dependencies {
    implementation(kotlin("stdlib"))

    //==================================================== 내부 의존성 ======================================================
    api(project(":core1")) //API로 해야 하위 프로젝트에서 사용 가능하다.

    //==================================================== javax ======================================================
    api("javax.validation:validation-api:2.0.1.Final")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") //자주 쓰니까 일단 넣음
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0") //LocalDatetime 등.. 상당히 별로임.. 맘에안듬.

    //==================================================== 로깅.. 맘에 안드네 ======================================================
    api("io.github.microutils:kotlin-logging-jvm:2.0.10") //slf4j의 래퍼. (로거 가져올때 사용)
    api("ch.qos.logback:logback-classic:1.4.5") //slf4j의 실제 구현체 (레벨 설정에 참조해야함)
    implementation("org.codehaus.janino:janino:3.1.9") //logback 파일롤링 표현식 필터처리에 필요함

    //==================================================== 코틀린 기본 ======================================================
    api("com.lectra:koson:1.2.4") // 코틀린 json DSL
    api("com.github.doyaaaaaken:kotlin-csv:1.6.0") //CSV.. 좀 신뢰가 안가는 이름이네. 87kb 로 매우 가벼움
    api("org.jetbrains.kotlinx:kotlinx-html:0.8.0")
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0") //간단 HTML 구성

    //==================================================== 기본옵션 ======================================================
    api("com.google.code.gson:gson:2.10") // 외부 의존성 없음.. 깔끔함.  300kb 이내  https://mvnrepository.com/artifact/com.google.code.gson/gson

    //==================================================== 공통 테스트를 위해서 implementation 의존성 추가 ======================================================
    implementation("org.junit-pioneer:junit-pioneer:1.9.1")  //환경변수 테스트용 (실서버 job 실행 등)
    implementation("org.junit.jupiter:junit-jupiter-api:5.9.1") //최신버전

    //==================================================== 테스트 (코테스트 샘플 추가) ======================================================
    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}

