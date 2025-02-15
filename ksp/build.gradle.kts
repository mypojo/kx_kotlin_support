//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
}


//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    implementation(kotlin("stdlib"))

    //==================================================== 내부 의존성 ======================================================
    api(project(":core"))

    //==================================================== KSP 관련 라이브러리 ======================================================
    implementation("com.squareup:kotlinpoet:_")
    implementation("com.squareup:kotlinpoet-ksp:_")

    val kotlinKspVersion: String by project
    implementation("com.google.devtools.ksp:symbol-processing-api:$kotlinKspVersion")


}
