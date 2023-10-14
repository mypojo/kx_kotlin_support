//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
}

//==================================================== 프로젝트별 설정 ======================================================

/** AWS_CDK 의존 패키지 */
dependencies {

    //==================================================== 내부 의존성 ======================================================
    api(project(":light"))
    testApi(project(":core").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== AWS ======================================================
    api("software.amazon.awscdk:aws-cdk-lib:2.93.0")   //https://mvnrepository.com/artifact/software.amazon.awscdk/aws-cdk-lib
    //api("software.constructs:constructs:10.1.278") //CDK 추가 빌딩블럭


}