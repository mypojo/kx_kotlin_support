//여기 한먼만 하면 별도 apply 필요없음
plugins {
    //코어 플러그인
    kotlin("jvm") //항상 최신버전 사용. 멀티플랫폼 버전과 동일함
}

//==================================================== 공통 ======================================================
/** 그래들 표준 문법을 간단하게 변경해줌 */
operator fun ProviderFactory.get(name: String): String = this.gradleProperty(name).get()
fun DependencyHandlerScope.parent(moduleName: String) {
    api(project(moduleName))
    testImplementation(project(":core2").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통을 사용할 수 있게 해줌
}

//==================================================== 프로젝트별 설정 ======================================================

dependencies {
    //==================================================== 내부 의존성 ======================================================
    api(project(":aws"))
    testApi(project(":core2").dependencyProject.sourceSets["test"].output) //코어 테스트에 있는 공통 (testRoot 등)을 사용할 수 있게 해줌

    //==================================================== 코틀린 & 젯브레인 시리즈 ======================================================
    api("org.jetbrains.kotlin:kotlin-reflect:${providers["kotlinVersion"]}") // 리플렉션 dto 변환용
    api("io.insert-koin:koin-core:3.4.0") //kotlin DI 도구  <-- module 단계서부터 적용
    implementation("org.jetbrains.exposed:exposed:${providers["exposedVersion"]}") //일단 api 로 안함. 활용성 보기

    //==================================================== RDB ======================================================
    //implementation("software.aws.rds:aws-mysql-jdbc:1.1.8") //aws 장애조치기능이 담긴 mysql 드라이버 & 모든 mysql과 호환가능. https://github.com/awslabs/aws-mysql-jdbc <-- 클러스터 설정등이 필요
    //implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4") //일반 접속용 마리아 DB. intellij에서 오로라 연결시 이게 디폴트인듯.
    implementation("com.mysql:mysql-connector-j:8.1.0") //이걸로 해야 IAM JDBC 연결됨.. 마리아로 하면 안됨. 뭐 추가옵션이 있는듯. (왜??)

    //==================================================== 기타 ======================================================
    api("com.jcraft:jsch:0.1.55") //SFTP 모듈
    api("org.apache.poi:poi-ooxml:5.2.3") //엑셀

    implementation("commons-codec:commons-codec:1.15") //구글 OTP 모듈
    //implementation("io.zeko:zeko-sql-builder:1.4.0") //스키마 정의 없는 SQL 빌더 (비정형 쿼리용 or 간단 람다 API 쿼리)
    implementation("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.5.0") //깃에서 주워옴. JDBC 간단래퍼
    api("com.amazonaws:dynamodb-lock-client:1.2.0") //DDB 분산락 클라이언트 정발버전


    //==================================================== 기타 ======================================================
    implementation("org.passay:passay:1.6.3") //패스워드 간단 검증
    implementation("com.google.ortools:ortools-java:9.6.2534") //구글 최적화도구 orTool https://developers.google.com/optimization/install/java/pkg_windows?hl=ko

    //==================================================== 기본 의존 ======================================================
    api("com.slack.api:slack-api-client:1.29.1") //기본 API만.  //implementation("com.slack.api:bolt-jetty:1.28.1")     // 기본  API 및 bolt-servlet 등을 포함한다

}
