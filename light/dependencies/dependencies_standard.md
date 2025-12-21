## JVM 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | jakarta.validation | jakarta.validation-api | {require 3.1.1; reject _} -> 3.1.1 | project::core |
| 컴파일 | javax.activation | activation | 1.1 | com.google.gdata:core -> javax.mail:mail |
| 컴파일 | javax.mail | mail | 1.4 | com.google.gdata:core |

## 사실상 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | ch.qos.logback | logback-classic | {require 1.5.18; reject _} -> 1.5.18 | project::core |
| 컴파일 | ch.qos.logback | logback-core | 1.5.18 | project::core -> ch.qos.logback:logback-classic |
| 컴파일 | com.github.doyaaaaaken | kotlin-csv | {require 1.10.0; reject _} -> 1.10.0 | project::core |
| 컴파일 | com.github.doyaaaaaken | kotlin-csv-jvm | 1.10.0 | project::core -> com.github.doyaaaaaken:kotlin-csv |
| 컴파일 | com.slack.api | slack-api-client | {require 1.45.3; reject _} -> 1.45.3 |  |
| 컴파일 | com.slack.api | slack-api-model | 1.45.3 | com.slack.api:slack-api-client<br>com.slack.api:slack-api-model-kotlin-extension |
| 컴파일 | com.slack.api | slack-api-model-kotlin-extension | {require 1.45.3; reject _} -> 1.45.3 |  |
| 컴파일 | com.squareup.okhttp3 | logging-interceptor | {require 5.0.0-alpha.16; reject _} -> 5.0.0-alpha.16 |  |
| 컴파일 | com.squareup.okhttp3 | okhttp | {require 5.0.0-alpha.16; reject _} -> 5.1.0 |  |
| 컴파일 | com.squareup.okhttp3 | okhttp | 4.12.0 -> 5.1.0 | com.slack.api:slack-api-client<br>com.squareup.retrofit2:retrofit |
| 컴파일 | com.squareup.okhttp3 | okhttp | 5.0.0-alpha.16 -> 5.1.0 | com.squareup.okhttp3:logging-interceptor |
| 컴파일 | com.squareup.okhttp3 | okhttp | 5.1.0 | io.ktor:ktor-client-okhttp-jvm |
| 컴파일 | com.squareup.okhttp3 | okhttp-jvm | 5.1.0 | com.squareup.okhttp3:okhttp |
| 컴파일 | com.squareup.okio | okio | 3.15.0 -> 3.16.0 | com.squareup.okhttp3:okhttp -> com.squareup.okhttp3:okhttp-jvm |
| 컴파일 | com.squareup.okio | okio | 3.16.0 | io.ktor:ktor-client-okhttp-jvm |
| 컴파일 | com.squareup.okio | okio-jvm | 3.16.0 | com.squareup.okhttp3:okhttp -> com.squareup.okhttp3:okhttp-jvm -> ... |
| 컴파일 | com.squareup.retrofit2 | converter-gson | {require 3.0.0; reject _} -> 3.0.0 |  |
| 컴파일 | com.squareup.retrofit2 | retrofit | {require 3.0.0; reject _} -> 3.0.0 |  |
| 컴파일 | com.squareup.retrofit2 | retrofit | 3.0.0 | com.squareup.retrofit2:converter-gson |
| 컴파일 | io.netty | netty-buffer | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-codec | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-codec-http | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-codec-http2 | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm |
| 컴파일 | io.netty | netty-common | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-handler | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-resolver | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-transport | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-transport-classes-epoll | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-transport-classes-kqueue | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.netty | netty-transport-native-epoll | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm |
| 컴파일 | io.netty | netty-transport-native-kqueue | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm |
| 컴파일 | io.netty | netty-transport-native-unix-common | 4.1.118.Final | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | joda-time | joda-time | 2.10.8 | com.amazonaws:aws-lambda-java-events |
| 컴파일 | org.junit | junit-bom | 5.12.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.junit.jupiter | junit-jupiter | 5.12.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.junit.jupiter | junit-jupiter-api | 5.12.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.junit.jupiter | junit-jupiter-params | 5.12.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.junit.platform | junit-platform-commons | 1.12.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.slf4j | slf4j-api | 2.0.3 -> 2.0.17 | project::core -> io.github.microutils:kotlin-logging-jvm |
| 컴파일 | org.slf4j | slf4j-api | 2.0.17 | project::core -> ch.qos.logback:logback-classic<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ...<br>io.ktor:ktor-client-okhttp-jvm |
| 컴파일 | org.slf4j | slf4j-api | 1.7.36 -> 2.0.17 | com.amazonaws:dynamodb-lock-client -> software.amazon.awssdk:dynamodb -> ...<br>com.slack.api:slack-api-client |
| 컴파일 | org.slf4j | slf4j-api | 2.0.16 -> 2.0.17 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.ktor:ktor-server-html-builder-jvm<br>io.ktor:ktor-server-auth-jvm<br>io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions -> ...<br>io.ktor:ktor-server-auth-jwt-jvm<br>io.ktor:ktor-server-auto-head-response-jvm<br>io.ktor:ktor-server-host-common-jvm<br>io.ktor:ktor-server-status-pages-jvm<br>io.ktor:ktor-client-auth -> io.ktor:ktor-client-auth-jvm |

## 사실상 표준의 의존성

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | com.fasterxml.jackson | jackson-bom | 2.15.4 | io.ktor:ktor-server-auth-jwt-jvm -> com.auth0:java-jwt -> ... |
| 컴파일 | com.fasterxml.jackson.core | jackson-annotations | 2.15.4 | io.ktor:ktor-server-auth-jwt-jvm -> com.auth0:java-jwt -> ... |
| 컴파일 | com.fasterxml.jackson.core | jackson-core | 2.15.4 | io.ktor:ktor-server-auth-jwt-jvm -> com.auth0:java-jwt<br>io.ktor:ktor-server-auth-jwt-jvm -> com.auth0:java-jwt -> ... |
| 컴파일 | com.fasterxml.jackson.core | jackson-databind | 2.15.4 | io.ktor:ktor-server-auth-jwt-jvm -> com.auth0:java-jwt -> ...<br>io.ktor:ktor-server-auth-jwt-jvm -> com.auth0:java-jwt |
| 컴파일 | commons-codec | commons-codec | 1.11 -> 1.17.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | commons-codec | commons-codec | 1.17.1 | software.amazon.awssdk:apache-client<br>com.google.apis:google-api-services-oauth2 -> com.google.api-client:google-api-client |

## 아파치 시리즈

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | org.apache.commons | commons-lang3 | 3.17.0 | org.apache.commons:commons-text |
| 컴파일 | org.apache.commons | commons-text | {require 1.13.0; reject _} -> 1.13.0 |  |
| 컴파일 | org.apache.httpcomponents | httpasyncclient | 4.1.5 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.apache.httpcomponents | httpclient | 4.5.13 -> 4.5.14 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>software.amazon.awssdk:apache-client |
| 컴파일 | org.apache.httpcomponents | httpclient | 4.5.14 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ...<br>com.google.apis:google-api-services-oauth2 -> com.google.api-client:google-api-client -> ...<br>com.google.apis:google-api-services-oauth2 -> com.google.api-client:google-api-client |
| 컴파일 | org.apache.httpcomponents | httpcore | 4.4.15 -> 4.4.16 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.apache.httpcomponents | httpcore | 4.4.16 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>software.amazon.awssdk:apache-client<br>com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ...<br>com.google.apis:google-api-services-oauth2 -> com.google.api-client:google-api-client -> ...<br>com.google.apis:google-api-services-oauth2 -> com.google.api-client:google-api-client |
| 컴파일 | org.apache.httpcomponents | httpcore-nio | 4.4.15 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |

