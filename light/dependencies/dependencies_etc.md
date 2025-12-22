## AI 관련

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | com.aallam.openai | openai-client | {require 4.0.1; reject _} -> 4.0.1 |  |
| 컴파일/런타임 | com.aallam.openai | openai-client-jvm | 4.0.1 | com.aallam.openai:openai-client |
| 컴파일/런타임 | com.aallam.openai | openai-core | 4.0.1 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm |
| 컴파일/런타임 | com.aallam.openai | openai-core-jvm | 4.0.1 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk | {require 0.7.2; reject _} -> 0.7.2 |  |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-client | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-client-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-core | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-core-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-server | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm |
| 컴파일/런타임 | io.modelcontextprotocol | kotlin-sdk-server-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |


## 기타

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | co.touchlab | stately-common | 1.1.10 | com.petersamokhin.notionsdk:notionsdk -> com.petersamokhin.notionsdk:notionsdk-jvm |
| 런타임 | co.touchlab | stately-common-jvm | 1.1.10 | com.petersamokhin.notionsdk:notionsdk -> com.petersamokhin.notionsdk:notionsdk-jvm -> ... |
| 런타임 | co.touchlab | stately-concurrency | 2.1.0 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm<br>io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ... |
| 런타임 | co.touchlab | stately-concurrency | 1.1.10 -> 2.1.0 | com.petersamokhin.notionsdk:notionsdk -> com.petersamokhin.notionsdk:notionsdk-jvm |
| 런타임 | co.touchlab | stately-concurrency-jvm | 2.1.0 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ... |
| 런타임 | co.touchlab | stately-concurrent-collections | 2.1.0 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm |
| 런타임 | co.touchlab | stately-concurrent-collections-jvm | 2.1.0 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ... |
| 런타임 | co.touchlab | stately-strict | 2.1.0 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ... |
| 런타임 | co.touchlab | stately-strict-jvm | 2.1.0 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ... |
| 컴파일/런타임 | com.amazonaws | aws-lambda-java-core | {require 1.2.3; reject _} -> 1.2.3 |  |
| 컴파일/런타임 | com.amazonaws | aws-lambda-java-events | {require 3.15.0; reject _} -> 3.15.0 |  |
| 컴파일/런타임 | com.amazonaws | dynamodb-lock-client | {require 1.4.0; reject _} -> 1.4.0 |  |
| 컴파일/런타임 | com.auth0 | java-jwt | 4.5.0 | io.ktor:ktor-server-auth-jwt-jvm |
| 컴파일/런타임 | com.auth0 | jwks-rsa | 0.22.1 | io.ktor:ktor-server-auth-jwt-jvm |
| 컴파일/런타임 | com.charleskorn.kaml | kaml | {require 0.82.0; reject _} -> 0.82.0 |  |
| 컴파일/런타임 | com.charleskorn.kaml | kaml-jvm | 0.82.0 | com.charleskorn.kaml:kaml |
| 컴파일/런타임 | com.microsoft.playwright | driver | 1.51.0 | com.microsoft.playwright:playwright<br>com.microsoft.playwright:playwright -> com.microsoft.playwright:driver-bundle |
| 컴파일/런타임 | com.microsoft.playwright | driver-bundle | 1.51.0 | com.microsoft.playwright:playwright |
| 컴파일/런타임 | com.microsoft.playwright | playwright | {require 1.51.0; reject _} -> 1.51.0 |  |
| 컴파일/런타임 | com.petersamokhin.notionsdk | notionsdk | {require 0.0.5; reject _} -> 0.0.5 |  |
| 컴파일/런타임 | com.petersamokhin.notionsdk | notionsdk-jvm | 0.0.5 | com.petersamokhin.notionsdk:notionsdk |
| 컴파일/런타임 | com.sun.activation | jakarta.activation | 2.0.1 | com.sun.mail:jakarta.mail |
| 컴파일/런타임 | com.sun.mail | jakarta.mail | {require 2.0.2; reject _} -> 2.0.2 |  |
| 컴파일/런타임 | com.typesafe | config | 1.4.4 | io.ktor:ktor-server-core-jvm |
| 컴파일/런타임 | gov.nist.math | jama | 1.0.3 |  |
| 컴파일/런타임 | io.github.crac | org-crac | {require 0.1.3; reject _} -> 0.1.3 |  |
| 런타임 | io.github.oshai | kotlin-logging | 7.0.13 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 런타임 | io.github.oshai | kotlin-logging-jvm | 7.0.13 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일/런타임 | io.opencensus | opencensus-api | 0.31.1 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일/런타임 | io.opencensus | opencensus-contrib-http-util | 0.31.1 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 런타임 | it.krzeminski | snakeyaml-engine-kmp | 3.1.1 | com.charleskorn.kaml:kaml -> com.charleskorn.kaml:kaml-jvm |
| 런타임 | it.krzeminski | snakeyaml-engine-kmp-jvm | 3.1.1 | com.charleskorn.kaml:kaml -> com.charleskorn.kaml:kaml-jvm -> ... |
| 런타임 | net.java.dev.jna | jna | 5.9.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 런타임 | net.java.dev.jna | jna-platform | 5.9.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일/런타임 | net.lingala.zip4j | zip4j | {require 2.11.5; reject _} -> 2.11.5 |  |
| 런타임 | net.thauvin.erik.urlencoder | urlencoder-lib | 1.6.0 | com.charleskorn.kaml:kaml -> com.charleskorn.kaml:kaml-jvm -> ... |
| 런타임 | net.thauvin.erik.urlencoder | urlencoder-lib-jvm | 1.6.0 | com.charleskorn.kaml:kaml -> com.charleskorn.kaml:kaml-jvm -> ... |
| 컴파일 | org.apiguardian | apiguardian-api | 1.1.2 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일/런타임 | org.eclipse.jetty.alpn | alpn-api | 1.1.3.v20160715 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm |
| 런타임 | org.fusesource.jansi | jansi | 2.4.2 | io.ktor:ktor-server-core-jvm |
| 런타임 | org.fusesource.jansi | jansi | 2.4.1 -> 2.4.2 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일/런타임 | org.jraf | klibnotion | {require 1.12.0; reject _} -> 1.12.0 |  |
| 컴파일/런타임 | org.jraf | klibnotion-jvm | 1.12.0 | org.jraf:klibnotion |
| 컴파일/런타임 | org.jsoup | jsoup | {require 1.19.1; reject _} -> 1.19.1 |  |
| 컴파일/런타임 | org.jspecify | jspecify | 1.0.0 | com.google.guava:guava |
| 컴파일/런타임 | org.mortbay.jetty | jetty | 6.1.26 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty |
| 컴파일/런타임 | org.mortbay.jetty | jetty-util | 6.1.26 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일/런타임 | org.mortbay.jetty | servlet-api | 2.5-20081211 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일/런타임 | org.opentest4j | opentest4j | 1.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>com.microsoft.playwright:playwright |
| 컴파일/런타임 | org.reactivestreams | reactive-streams | 1.0.4 | com.amazonaws:dynamodb-lock-client -> software.amazon.awssdk:dynamodb -> ... |
