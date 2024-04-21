//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization")
}

//==================================================== 프로젝트별 설정 ======================================================

tasks.getByName<Test>("test") {
    //코테스트 샘플 실행 제거
//    exclude("**/string/**") //코테스트 제외
//    exclude("**/concurrent/**") //코테스트 제외

    useJUnitPlatform{
        //includeTags("L1")
        //systemProperty("kotest.tags.include", "L2")

        include("**/net/kotlinx/core/string/*")
        includeTags("L2")

        testLogging {
            events("passed", "skipped", "failed")
        }// e test 의

        //kotest 설정 (JUnit 아님!)
        maxHeapSize = "4096m"  //gradle test 의 경우 JVM 옵션이 아니라 여기 설정해야함.. (왜인지 모르겠음)
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    //==================================================== jakarta ======================================================
    api("jakarta.validation:jakarta.validation-api:_")

    //==================================================== jetbrain ======================================================
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:_") //코투틴 기본으로 적용
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:_") //자주 쓰니까 일단 넣음
    //api("org.jetbrains.kotlinx:kotlinx-datetime:_") //LocalDatetime 등 serial & 멀티플랫폼 위해서 kotlin datetime 사용 -> 사용법 너무 틀려서 금지

    //==================================================== 코틀린 기본 ======================================================
    api("com.lectra:koson:_") // 코틀린 json DSL
    api("com.github.doyaaaaaken:kotlin-csv:_") //CSV.. 좀 신뢰가 안가는 이름이네. 87kb 로 매우 가벼움
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:_") //간단 HTML 구성

    //==================================================== 기본옵션 ======================================================
    api("com.google.code.gson:gson:_") // 외부 의존성 없음.. 깔끔함.  300kb 이내  https://mvnrepository.com/artifact/com.google.code.gson/gson

    //==================================================== 라인의 컨디셔널 (합쳐서 100kb) ======================================================
    runtimeOnly("com.linecorp.conditional:conditional:_")
    api("com.linecorp.conditional:conditional-kotlin:_") //요구사항에 and 와 or이 포함되어있을경우 이걸로 예쁘게 포매팅

    //==================================================== 로깅.. 맘에 안드네 ======================================================
    api("io.github.microutils:kotlin-logging-jvm:_") //slf4j의 래퍼. (로거 가져올때 사용)
    api("ch.qos.logback:logback-classic:_") //slf4j의 실제 구현체 (레벨 설정에 참조해야함)
    api("org.codehaus.janino:janino:_") //logback 파일롤링 표현식 필터처리에 필요함

    testImplementation("io.kotest:kotest-runner-junit5:_") //kotest 의존성 추가.  현재버전의경우 scan 이슈가 있어서 프로퍼티 설정 해줘야함

}

