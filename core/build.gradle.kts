//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization")
}

//==================================================== 프로젝트별 설정 ======================================================

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

    //==================================================== 벨리데이션  ======================================================
    api("io.konform:konform-jvm:_") //코틀린 필드 벨리데이션
    runtimeOnly("com.linecorp.conditional:conditional:_") //라인의 컨디셔널 (합쳐서 100kb)
    api("com.linecorp.conditional:conditional-kotlin:_") //요구사항에 and 와 or이 포함되어있을경우 이걸로 예쁘게 포매팅

    //==================================================== 로깅 ======================================================
    api("io.github.microutils:kotlin-logging-jvm:_") //slf4j의 래퍼. (로거 가져올때 사용)
    api("ch.qos.logback:logback-classic:_") //slf4j의 실제 구현체 (레벨 설정에 참조해야함)
    api("org.codehaus.janino:janino:_") //logback 파일롤링 표현식 필터처리에 필요함

}

