//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
}

//==================================================== 프로젝트별 설정 ======================================================

dependencies {

    //==================================================== 내부 의존성 ======================================================
    api(project(":heavy_boot3"))

    //==================================================== 테스트용 도구 추가 ======================================================
    implementation("com.google.ortools:ortools-java:_") //구글 최적화도구 orTool https://developers.google.com/optimization/install/java/pkg_windows?hl=ko

    //==================================================== 테스트 api   ======================================================
    api("org.junit-pioneer:junit-pioneer:_")  //환경변수 테스트용 (실서버 job 실행 등)
    api("org.junit.jupiter:junit-jupiter-api:_") //최신버전
    api("io.mockk:mockk:_") //코틀린 모킹

    //api("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:0.6.10")  //네이버 픽스쳐몽키.. 애매함 -> .jqwik-database 이파일 자꾸 생겨서 제거

    //==================================================== 머신러닝 ======================================================
    api("org.deeplearning4j:deeplearning4j-core:_")  //https://deeplearning4j.konduit.ai/
    api("org.deeplearning4j:deeplearning4j-nlp:_") //NLP(Natural Language Processing, 자연어 처리)
    api("org.nd4j:nd4j-native-platform:_")  //ND4J는 다차원 행렬 계산을 실행하는 API를 제공



    //==================================================== 코테스트 단위테스트용 ======================================================
    //코테스트 문제가 많아서 공통에서 제거함 -> 이유없이 메모리아웃 오류남
    //코테스트 넘으면 너무 느려져서 일단 제거
//    testImplementation("io.kotest:kotest-runner-junit5:_")
//    testImplementation("io.kotest:kotest-assertions-core:_")


}