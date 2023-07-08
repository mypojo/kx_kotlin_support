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
    implementation("org.jetbrains.exposed:exposed:${providers["exposedVersion"]}")
    api("io.insert-koin:koin-core:3.4.0") //kotlin DI 도구  <-- module 단계서부터 적용
    //implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
//        implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
//        implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
//        implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    //==================================================== RDB ======================================================
    implementation("software.aws.rds:aws-mysql-jdbc:1.1.2") //aws 장애조치기능이 담긴 mysql 드라이버 & 모든 mysql과 호환가능. https://github.com/awslabs/aws-mysql-jdbc
    implementation("com.zaxxer:HikariCP:5.0.1")

    //==================================================== RDS IAM ======================================================
    val awsSdk2Version = "2.17.118" //사용중인거하고 맞춤
    implementation("software.amazon.awssdk:rds:$awsSdk2Version") //AWS SDK2 버전의 IAM 데이터소스 (코틀린 버전 없음)
    implementation("software.amazon.awssdk:sts:$awsSdk2Version") //IAM 토큰 발행시 필요

    //==================================================== 기타 ======================================================
    api("com.jcraft:jsch:0.1.55") //SFTP 모듈
    api("org.apache.poi:poi-ooxml:4.1.0") //엑셀
    implementation("commons-codec:commons-codec:1.15") //구글 OTP 모듈
    //==================================================== 기타 ======================================================
    implementation("org.passay:passay:1.6.3") //패스워드 간단 검증


    //==================================================== 기본 의존 ======================================================
    api("com.google.guava:guava:31.1-jre")  //AWS에도 동일의존 있음
    api("com.slack.api:slack-api-client:1.29.1") //기본 API만.  //implementation("com.slack.api:bolt-jetty:1.28.1")     // 기본  API 및 bolt-servlet 등을 포함한다

}
