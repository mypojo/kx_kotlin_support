//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()

//==================================================== 프로젝트별 설정 ======================================================

dependencies {

    //==================================================== 내부 의존성 ======================================================
    api(project(":heavy"))
    testApi(project(":core").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌


    //==================================================== 테스트용 도구 추가 ======================================================

    implementation("com.google.ortools:ortools-java:9.6.2534") //구글 최적화도구 orTool https://developers.google.com/optimization/install/java/pkg_windows?hl=ko



}