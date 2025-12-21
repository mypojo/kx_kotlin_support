## AI 관련

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | com.aallam.openai | openai-client | {require 4.0.1; reject _} -> 4.0.1 |  |
| 컴파일 | com.aallam.openai | openai-client-jvm | 4.0.1 | com.aallam.openai:openai-client |
| 컴파일 | com.aallam.openai | openai-core | 4.0.1 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm |
| 컴파일 | com.aallam.openai | openai-core-jvm | 4.0.1 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk | {require 0.7.2; reject _} -> 0.7.2 |  |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-client | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-client-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-core | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-core-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-server | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm |
| 컴파일 | io.modelcontextprotocol | kotlin-sdk-server-jvm | 0.7.2 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |

## 기타

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | com.auth0 | java-jwt | 4.5.0 | io.ktor:ktor-server-auth-jwt-jvm |
| 컴파일 | com.auth0 | jwks-rsa | 0.22.1 | io.ktor:ktor-server-auth-jwt-jvm |
| 컴파일 | com.charleskorn.kaml | kaml | {require 0.82.0; reject _} -> 0.82.0 |  |
| 컴파일 | com.charleskorn.kaml | kaml-jvm | 0.82.0 | com.charleskorn.kaml:kaml |
| 컴파일 | com.jayway.jsonpath | json-path | {require 2.9.0; reject _} -> 2.9.0 | project::core |
| 컴파일 | com.lectra | koson | {require 1.2.9; reject _} -> 1.2.9 | project::core |
| 컴파일 | com.linecorp.conditional | conditional-kotlin | {require 1.1.3; reject _} -> 1.1.3 | project::core |
| 컴파일 | com.microsoft.playwright | driver | 1.51.0 | com.microsoft.playwright:playwright<br>com.microsoft.playwright:playwright -> com.microsoft.playwright:driver-bundle |
| 컴파일 | com.microsoft.playwright | driver-bundle | 1.51.0 | com.microsoft.playwright:playwright |
| 컴파일 | com.microsoft.playwright | playwright | {require 1.51.0; reject _} -> 1.51.0 |  |
| 컴파일 | com.petersamokhin.notionsdk | notionsdk | {require 0.0.5; reject _} -> 0.0.5 |  |
| 컴파일 | com.petersamokhin.notionsdk | notionsdk-jvm | 0.0.5 | com.petersamokhin.notionsdk:notionsdk |
| 컴파일 | com.sun.activation | jakarta.activation | 2.0.1 | com.sun.mail:jakarta.mail |
| 컴파일 | com.sun.mail | jakarta.mail | {require 2.0.2; reject _} -> 2.0.2 |  |
| 컴파일 | com.typesafe | config | 1.4.4 | io.ktor:ktor-server-core-jvm |
| 컴파일 | gov.nist.math | jama | 1.0.3 |  |
| 컴파일 | io.github.crac | org-crac | {require 0.1.3; reject _} -> 0.1.3 |  |
| 컴파일 | io.github.microutils | kotlin-logging-jvm | {require 3.0.5; reject _} -> 3.0.5 | project::core |
| 컴파일 | io.insert-koin | koin-core | {require 4.1.0-RC1; reject _} -> 4.1.0-RC1 |  |
| 컴파일 | io.insert-koin | koin-core-annotations | 4.1.0-RC1 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm |
| 컴파일 | io.insert-koin | koin-core-annotations-jvm | 4.1.0-RC1 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ... |
| 컴파일 | io.insert-koin | koin-core-jvm | 4.1.0-RC1 | io.insert-koin:koin-core |
| 컴파일 | io.opencensus | opencensus-api | 0.31.1 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일 | io.opencensus | opencensus-contrib-http-util | 0.31.1 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일 | net.lingala.zip4j | zip4j | {require 2.11.5; reject _} -> 2.11.5 |  |
| 컴파일 | org.apiguardian | apiguardian-api | 1.1.2 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.codehaus.janino | commons-compiler | 3.1.12 | project::core -> org.codehaus.janino:janino |
| 컴파일 | org.codehaus.janino | janino | {require 3.1.12; reject _} -> 3.1.12 | project::core |
| 컴파일 | org.eclipse.jetty.alpn | alpn-api | 1.1.3.v20160715 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm |
| 컴파일 | org.jraf | klibnotion | {require 1.12.0; reject _} -> 1.12.0 |  |
| 컴파일 | org.jraf | klibnotion-jvm | 1.12.0 | org.jraf:klibnotion |
| 컴파일 | org.jsoup | jsoup | {require 1.19.1; reject _} -> 1.19.1 |  |
| 컴파일 | org.jspecify | jspecify | 1.0.0 | com.google.guava:guava |
| 컴파일 | org.mortbay.jetty | jetty | 6.1.26 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty |
| 컴파일 | org.mortbay.jetty | jetty-util | 6.1.26 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일 | org.mortbay.jetty | servlet-api | 2.5-20081211 | com.google.gdata:core -> com.google.oauth-client:google-oauth-client-jetty -> ... |
| 컴파일 | org.opentest4j | opentest4j | 1.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>com.microsoft.playwright:playwright |
| 컴파일 | org.reactivestreams | reactive-streams | 1.0.4 | com.amazonaws:dynamodb-lock-client -> software.amazon.awssdk:dynamodb -> ... |

