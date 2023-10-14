//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization")
}

apply {
    plugin("org.jetbrains.kotlin.plugin.serialization")
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    //==================================================== 내부 의존성 ======================================================
    api(project(":light"))
    testApi(project(":core").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== 구글 ======================================================
    //사용하기 키 발급받아서 사용하기 너무 불편함!! 일단 사용처는 없음
    //implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") //https://github.com/googleapis/google-auth-library-java  구글인증은 이걸로 다 바뀐듯함 -> 나는 안씀
    implementation("com.google.gdata:core:1.47.1") //구글 기본세트
    implementation("com.google.apis:google-api-services-oauth2:v2-rev151-1.25.0") //구글 기본세트
    
    implementation("com.google.apis:google-api-services-calendar:v3-rev411-1.25.0") //캘린더
    implementation("com.google.apis:google-api-services-sheets:v4-rev581-1.25.0") //구글시트

}

/** 용량 확인용 fatJar 생성도구 추가 */
tasks.create("fatJar", Jar::class) {
    group = "build"
    description = "for aws lambda"
    manifest.attributes["Main-Class"] = "com.example.MyMainClass" //AWS 람다 등록시 필요없음
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)
    with(tasks.jar.get())
    archiveFileName = "fatJar.jar"
    isZip64 = true //archive contains more than 65535 entries
}