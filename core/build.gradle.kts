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
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") //코투틴 기본으로 적용

    //==================================================== jakarta ======================================================
    api("jakarta.validation:jakarta.validation-api:3.0.2")

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") //자주 쓰니까 일단 넣음
    //api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0") //LocalDatetime 등.. 상당히 별로임.. 일단 disable

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
    implementation("com.google.code.gson:gson:2.10") // 외부 의존성 없음.. 깔끔함.  300kb 이내  https://mvnrepository.com/artifact/com.google.code.gson/gson

    //==================================================== 라인의 컨디셔널 (합쳐서 100kb) ======================================================
    runtimeOnly("com.linecorp.conditional:conditional:1.1.3")
    api("com.linecorp.conditional:conditional-kotlin:1.1.3") //요구사항에 and 와 or이 포함되어있을경우 이걸로 예쁘게 포매팅


    //==================================================== 코테스트 단위테스트용 ======================================================
    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")

}

