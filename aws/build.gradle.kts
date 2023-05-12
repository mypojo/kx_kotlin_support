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
    api(project(":aws1"))
    testApi(project(":core2").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== 자바 표준 ======================================================
    api("javax.mail:javax.mail-api:1.6.2") //이메일 전송에 필요함

    //==================================================== 기본 (이정도는 괜찮겠지) ======================================================
    api("com.google.guava:guava:${providers["guavaVersion"]}")

    //==================================================== AWS ======================================================
    val awsVersion: String by project
    api("aws.sdk.kotlin:athena:$awsVersion")
    api("aws.sdk.kotlin:sts:$awsVersion")
    api("aws.sdk.kotlin:lambda:$awsVersion")
    api("aws.sdk.kotlin:iam:$awsVersion")
    api("aws.sdk.kotlin:rds:$awsVersion")
    api("aws.sdk.kotlin:ecs:$awsVersion")  //ec2 생략
    api("aws.sdk.kotlin:ses:$awsVersion")
    api("aws.sdk.kotlin:batch:$awsVersion")
    api("aws.sdk.kotlin:ssm:$awsVersion")
    api("aws.sdk.kotlin:eventbridge:$awsVersion")
    api("aws.sdk.kotlin:sfn:$awsVersion")
    api("aws.sdk.kotlin:codedeploy:$awsVersion")
    api("aws.sdk.kotlin:codecommit:$awsVersion")
    api("aws.sdk.kotlin:secretsmanager:$awsVersion")
    api("aws.sdk.kotlin:ec2:$awsVersion")
    api("aws.sdk.kotlin:ecr:$awsVersion")

    //덜중요한것들
    api("aws.sdk.kotlin:costexplorer:$awsVersion") //계정당 비용 확인용
//    api("aws.sdk.kotlin:pricing:$awsVersion") //제품의 비용(고정) 확인용
//    api("aws.sdk.kotlin:xray:$awsVersion")
//    api("aws.sdk.kotlin:elasticloadbalancingv2:$awsVersion")
//    api("aws.sdk.kotlin:quicksight:$awsVersion")
//    api("aws.sdk.kotlin:auth:$awsVersion")
//    api("aws.sdk.kotlin:regions:$awsVersion")

    //====================================================커먼즈 ======================================================
    implementation("org.apache.commons:commons-text:1.10.0") // javacript 등의 이스케이핑에 사용된다. kotlin 네이티브가 없네..


//        //==================================================== AWS 이벤트 스키마 바인딩에 필요 (안씀.. 너무 구림) ======================================================
//        implementation("com.fasterxml.jackson.core:jackson-core:2.10.0")
//        implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
//        implementation("com.fasterxml.jackson.core:jackson-annotations:2.10.0")

}
