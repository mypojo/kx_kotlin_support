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
    //api("io.mockk:mockk:_") //코틀린 모킹
    //api("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:0.6.10")  //네이버 픽스쳐몽키.. 애매함 -> .jqwik-database 이파일 자꾸 생겨서 제거

    //==================================================== 머신러닝 ======================================================
    api("org.deeplearning4j:deeplearning4j-core:_")  //https://deeplearning4j.konduit.ai/
    api("org.deeplearning4j:deeplearning4j-nlp:_") //NLP(Natural Language Processing, 자연어 처리)
    api("org.nd4j:nd4j-native-platform:_")  //ND4J는 다차원 행렬 계산을 실행하는 API를 제공

    api("gov.nist.math:jama:1.0.3") //https://mvnrepository.com/artifact/gov.nist.math/jama 회귀분석 패키지


    //==================================================== 테스트 (하위에서 사용하기 위해서 API로 등록) ======================================================
    api("io.kotest:kotest-runner-junit5:_") //kotest 의존성 추가.  현재버전의경우 scan 이슈가 있어서 프로퍼티 설정 해줘야함
    api("io.kotest.extensions:kotest-extensions-koin:_") //kotest koin 확장


    //==================================================== 크롤링 ======================================================
    api("com.github.shin285:KOMORAN:3.3.9") //간이 형태소분석기   https://docs.komoran.kr/firststep/installation.html

}