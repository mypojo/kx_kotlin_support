//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
    kotlin("plugin.serialization")
}

apply {
    plugin("org.jetbrains.kotlin.plugin.serialization")
}

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    //==================================================== 내부 의존성 ======================================================
    api(project(":light"))

    //==================================================== 자바 표준 ======================================================
    api("jakarta.mail:jakarta.mail-api:_") //이메일 전송에 필요함

    //==================================================== AWS ======================================================
    val awsVersion: String by project
    api("aws.sdk.kotlin:iam:$awsVersion") //용량 큼
    api("aws.sdk.kotlin:rds:$awsVersion")
    api("aws.sdk.kotlin:codedeploy:$awsVersion")
    api("aws.sdk.kotlin:secretsmanager:$awsVersion")
    api("aws.sdk.kotlin:ec2:$awsVersion") //용량 큼. 27 mbyte
    api("aws.sdk.kotlin:ecr:$awsVersion")

    //덜중요한것들
    api("aws.sdk.kotlin:costexplorer:$awsVersion") //계정당 비용 확인용 & 카테고리 태그 달기
    api("aws.sdk.kotlin:budgets:$awsVersion") //예산 설정
    api("aws.sdk.kotlin:elasticloadbalancingv2:$awsVersion") //레거시 수동 ALB 컨트롤용
    api("aws.sdk.kotlin:cloudfront:$awsVersion") //static hosting 배포후 캐시 정리용
    api("aws.sdk.kotlin:lakeformation:$awsVersion") //레이크 포메이션 테그생성 등
    api("aws.sdk.kotlin:glue:$awsVersion") //글루 데이터베이스 생성 등
    api("aws.sdk.kotlin:quicksight:$awsVersion") //퀵사이트

//    api("aws.sdk.kotlin:pricing:$awsVersion") //제품의 비용(고정) 확인용
//    api("aws.sdk.kotlin:xray:$awsVersion")
//    api("aws.sdk.kotlin:regions:$awsVersion")

    //==================================================== AWS 이벤트 스키마 바인딩에 필요 (안씀.. 너무 구림) ======================================================
//        implementation("com.fasterxml.jackson.core:jackson-core:2.10.0")
//        implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
//        implementation("com.fasterxml.jackson.core:jackson-annotations:2.10.0")

    //JAVA V2 RDS 는 kotlin 대체품이 나와서 의존성 제거
//    //==================================================== AWS JAVA V2 RDS IAM ======================================================
//    val awsJavaV2Version: String by project
//    implementation("software.amazon.awssdk:rds:$awsJavaV2Version") //AWS SDK2 버전의 IAM 데이터소스 (코틀린 버전 없음)
//    implementation("software.amazon.awssdk:sts:$awsJavaV2Version") //IAM 토큰 발행시 필요

    //==================================================== AWS CDK  ======================================================
    api("software.amazon.awscdk:aws-cdk-lib:_")   //https://mvnrepository.com/artifact/software.amazon.awscdk/aws-cdk-lib
    //api("software.constructs:constructs:10.1.278") //CDK 추가 빌딩블럭 -> 쓸만한게 없음

    //==================================================== 코틀린 & 젯브레인 시리즈 ======================================================
    api("org.jetbrains.exposed:exposed:_") //라이트 ORM
    api("org.jetbrains.kotlinx:dataframe:_") //주피터 노트북 코틀린버전.  리프레시 버전 적용

    //==================================================== RDB ======================================================
    //implementation("software.amazon.jdbc:aws-advanced-jdbc-wrapper:_") //https://github.com/aws/aws-advanced-jdbc-wrapper/  공식 드라이버 래퍼. (토큰관리 등등 내장)
    implementation("com.mysql:mysql-connector-j:_") //이걸로 해야 IAM JDBC 연결됨.. 마리아로 하면 안됨. 뭐 추가옵션이 있는듯. (왜??)
    api("com.zaxxer:HikariCP:_")  //최신버전 쓰면 Access denied 발생함.  5.1.0 으로 고정
    //api("com.zaxxer:HikariCP:6.3.0")

    //==================================================== RDB 도구 ======================================================
    //implementation("io.zeko:zeko-sql-builder:1.4.0") //스키마 정의 없는 SQL 빌더 (비정형 쿼리용 or 간단 람다 API 쿼리)
    api("com.vladsch.kotlin-jdbc:kotlin-jdbc:_") //깃에서 주워옴. JDBC 간단래퍼

    api("com.linecorp.kotlin-jdsl:jpql-dsl:_")  //라인에서 만든 코틀린 JPQL
    api("com.linecorp.kotlin-jdsl:jpql-render:_")  //JPQL 쿼리를 문자열로 렌더링
    api("com.linecorp.kotlin-jdsl:spring-data-jpa-support:_")


    //==================================================== 기타 ======================================================
    api("com.dropbox.core:dropbox-core-sdk:_") //드랍박스!

    api("org.passay:passay:_") //패스워드 간단 검증

    api("com.jcraft:jsch:_") //SFTP 모듈
    api("org.apache.poi:poi-ooxml:_") //엑셀

    //implementation("commons-codec:commons-codec:_") //구글 OTP 모듈 <-- 뭔지 기억이 안남.. 일단 삭제

}