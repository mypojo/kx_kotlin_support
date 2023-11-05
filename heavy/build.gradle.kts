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
    testApi(project(":light").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== 자바 표준 ======================================================
    api("jakarta.mail:jakarta.mail-api:2.1.2") //이메일 전송에 필요함

    //==================================================== AWS ======================================================
    val awsVersion: String by project
    api("aws.sdk.kotlin:sts:$awsVersion")
    api("aws.sdk.kotlin:iam:$awsVersion")
    api("aws.sdk.kotlin:rds:$awsVersion")
    api("aws.sdk.kotlin:ecs:$awsVersion")  //ec2 생략
    api("aws.sdk.kotlin:ses:$awsVersion")
    api("aws.sdk.kotlin:eventbridge:$awsVersion")
    api("aws.sdk.kotlin:codedeploy:$awsVersion")
    api("aws.sdk.kotlin:codecommit:$awsVersion")
    api("aws.sdk.kotlin:secretsmanager:$awsVersion")
    api("aws.sdk.kotlin:ec2:$awsVersion") //용량 큼
    api("aws.sdk.kotlin:ecr:$awsVersion")
    api("aws.sdk.kotlin:cloudwatchlogs:$awsVersion")

    //덜중요한것들
    api("aws.sdk.kotlin:costexplorer:$awsVersion") //계정당 비용 확인용
    api("aws.sdk.kotlin:elasticloadbalancingv2:$awsVersion") //레거시 수동 ALB 컨트롤용
//    api("aws.sdk.kotlin:pricing:$awsVersion") //제품의 비용(고정) 확인용
//    api("aws.sdk.kotlin:xray:$awsVersion")
//    api("aws.sdk.kotlin:quicksight:$awsVersion")
//    api("aws.sdk.kotlin:auth:$awsVersion")
//    api("aws.sdk.kotlin:regions:$awsVersion")

        //==================================================== AWS 이벤트 스키마 바인딩에 필요 (안씀.. 너무 구림) ======================================================
//        implementation("com.fasterxml.jackson.core:jackson-core:2.10.0")
//        implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
//        implementation("com.fasterxml.jackson.core:jackson-annotations:2.10.0")

    //==================================================== AWS JAVA V2 client (레거시 호환) ======================================================
    val awsJavaV2Version: String by project
    implementation("software.amazon.awssdk:apache-client:$awsJavaV2Version") //기본 HTTP 클라이언트 (쓰던거 씀)
    implementation("software.amazon.awssdk:dynamodb:$awsJavaV2Version") //DDB 분산락 작용용

    //==================================================== AWS JAVA V2 RDS IAM ======================================================
    implementation("software.amazon.awssdk:rds:$awsJavaV2Version") //AWS SDK2 버전의 IAM 데이터소스 (코틀린 버전 없음)
    implementation("software.amazon.awssdk:sts:$awsJavaV2Version") //IAM 토큰 발행시 필요

    //==================================================== 코틀린 & 젯브레인 시리즈 ======================================================
    api("org.jetbrains.exposed:exposed:${providers["exposedVersion"]}") //라이트 ORM
    api("org.jetbrains.kotlinx:dataframe:1727") //주피터 노트북 코틀린버전.  버전 이름이 이상하다.. 갑자기 이러네

    //==================================================== RDB ======================================================
    api("com.zaxxer:HikariCP:5.0.1")

    //implementation("software.aws.rds:aws-mysql-jdbc:1.1.8") //aws 장애조치기능이 담긴 mysql 드라이버 & 모든 mysql과 호환가능. https://github.com/awslabs/aws-mysql-jdbc <-- 클러스터 설정등이 필요
    //implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4") //일반 접속용 마리아 DB. intellij에서 오로라 연결시 이게 디폴트인듯.
    implementation("com.mysql:mysql-connector-j:8.1.0") //이걸로 해야 IAM JDBC 연결됨.. 마리아로 하면 안됨. 뭐 추가옵션이 있는듯. (왜??)

    //==================================================== 기타 ======================================================
    implementation("org.passay:passay:1.6.3") //패스워드 간단 검증

    api("com.jcraft:jsch:0.1.55") //SFTP 모듈
    api("org.apache.poi:poi-ooxml:5.2.3") //엑셀

    implementation("commons-codec:commons-codec:1.15") //구글 OTP 모듈
    //implementation("io.zeko:zeko-sql-builder:1.4.0") //스키마 정의 없는 SQL 빌더 (비정형 쿼리용 or 간단 람다 API 쿼리)
    implementation("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.5.0") //깃에서 주워옴. JDBC 간단래퍼
    api("com.amazonaws:dynamodb-lock-client:1.2.0") //DDB 분산락 클라이언트 정발버전

}