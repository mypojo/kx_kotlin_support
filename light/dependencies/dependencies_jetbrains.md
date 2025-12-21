## 젯브레인 ktor

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | io.ktor | ktor-client-apache | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | io.ktor | ktor-client-apache-jvm | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-client-auth | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-client-auth-jvm | 3.1.1 | io.ktor:ktor-client-auth |
| 컴파일 | io.ktor | ktor-client-cio | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | io.ktor | ktor-client-cio-jvm | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-client-core | 3.0.0 -> 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm |
| 컴파일 | io.ktor | ktor-client-core | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-auth-jvm<br>io.ktor:ktor-client-auth -> io.ktor:ktor-client-auth-jvm |
| 컴파일 | io.ktor | ktor-client-core | 3.3.0 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ...<br>io.ktor:ktor-client-okhttp-jvm |
| 컴파일 | io.ktor | ktor-client-core | {require 3.1.1; reject _} -> 3.3.0 |  |
| 컴파일 | io.ktor | ktor-client-core-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-client-okhttp-jvm | 3.3.0 |  |
| 컴파일 | io.ktor | ktor-events | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm |
| 컴파일 | io.ktor | ktor-events-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-http | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm |
| 컴파일 | io.ktor | ktor-http | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-http-cio | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm |
| 컴파일 | io.ktor | ktor-http-cio | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-http-cio-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-http-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-io | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-io-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-network | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-network | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-network-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-network-tls | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-network-tls-certificates | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | io.ktor | ktor-network-tls-certificates-jvm | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-network-tls-jvm | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-serialization | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm |
| 컴파일 | io.ktor | ktor-serialization-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-auth | 3.1.1 | io.ktor:ktor-server-auth-jwt-jvm |
| 컴파일 | io.ktor | ktor-server-auth-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-auth-jvm | 3.1.1 | io.ktor:ktor-server-auth-jwt-jvm -> io.ktor:ktor-server-auth |
| 컴파일 | io.ktor | ktor-server-auth-jwt-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-auto-head-response-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-call-logging | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | io.ktor | ktor-server-call-logging-jvm | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-core | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-html-builder-jvm<br>io.ktor:ktor-server-auth-jvm<br>io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions -> ...<br>io.ktor:ktor-server-auth-jwt-jvm<br>io.ktor:ktor-server-auto-head-response-jvm<br>io.ktor:ktor-server-host-common-jvm<br>io.ktor:ktor-server-status-pages-jvm |
| 컴파일 | io.ktor | ktor-server-core | 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-core-jvm | {require 3.1.1; reject _} -> 3.3.0 |  |
| 컴파일 | io.ktor | ktor-server-core-jvm | 3.3.0 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-host-common-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-html-builder-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-netty | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-netty-jvm | 3.1.1 | io.ktor:ktor-server-netty |
| 컴파일 | io.ktor | ktor-server-sessions | 3.1.1 | io.ktor:ktor-server-auth-jvm |
| 컴파일 | io.ktor | ktor-server-sessions-jvm | 3.1.1 | io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions |
| 컴파일 | io.ktor | ktor-server-sessions-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-sse | 3.3.0 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-sse-jvm | 3.3.0 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-status-pages-jvm | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-test-host | {require 3.1.1; reject _} -> 3.1.1 |  |
| 컴파일 | io.ktor | ktor-server-test-host-jvm | 3.1.1 | io.ktor:ktor-server-test-host |
| 컴파일 | io.ktor | ktor-server-websockets | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | io.ktor | ktor-server-websockets | 3.3.0 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.ktor | ktor-server-websockets-jvm | 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-sse | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | io.ktor | ktor-sse-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-test-dispatcher | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | io.ktor | ktor-test-dispatcher-jvm | 3.1.1 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-utils | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm |
| 컴파일 | io.ktor | ktor-utils | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-utils-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-websocket-serialization | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-websocket-serialization-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | io.ktor | ktor-websockets | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-websockets | 3.1.1 -> 3.3.0 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | io.ktor | ktor-websockets-jvm | 3.3.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |

## 젯브레인 기타

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일 | org.jetbrains | annotations | 23.0.0 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.jetbrains | annotations | 13.0 -> 23.0.0 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-reflect | 2.2.0 -> 2.2.10 |  |
| 컴파일 | org.jetbrains.kotlin | kotlin-reflect | 2.2.10 | io.ktor:ktor-server-core-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.1.0 -> 2.2.10 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.2.0 -> 2.2.10 | project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json -> ...<br>com.squareup.okhttp3:okhttp -> com.squareup.okhttp3:okhttp-jvm -> ...<br>com.squareup.okhttp3:okhttp -> com.squareup.okhttp3:okhttp-jvm<br>aws.sdk.kotlin:s3 -> aws.sdk.kotlin:s3-jvm -> ...<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>aws.smithy.kotlin:http-client-engine-okhttp-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.2.10 | project::core -> com.github.doyaaaaaken:kotlin-csv -> ...<br>org.jetbrains.kotlin:kotlin-reflect<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm<br>io.ktor:ktor-client-okhttp-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 1.9.10 -> 2.2.10 | project::core -> com.github.doyaaaaaken:kotlin-csv -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.0.21 -> 2.2.10 | project::core -> org.jetbrains.kotlinx:kotlinx-html-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.1.20 -> 2.2.10 | io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm -> ...<br>io.insert-koin:koin-core -> io.insert-koin:koin-core-jvm<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 1.9.24 -> 2.2.10 | com.slack.api:slack-api-model-kotlin-extension |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.0.20 -> 2.2.10 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.1.10 -> 2.2.10 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-html-builder-jvm<br>io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions -> ...<br>io.ktor:ktor-server-auth-jvm<br>io.ktor:ktor-server-auth-jwt-jvm<br>io.ktor:ktor-server-auto-head-response-jvm<br>io.ktor:ktor-server-host-common-jvm<br>io.ktor:ktor-server-status-pages-jvm<br>io.ktor:ktor-client-auth -> io.ktor:ktor-client-auth-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib | 2.1.21 -> 2.2.10 | com.squareup.retrofit2:retrofit<br>com.squareup.okhttp3:logging-interceptor<br>com.charleskorn.kaml:kaml -> com.charleskorn.kaml:kaml-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib -> 2.2.10 |  |  |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-common | 2.2.10 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-common | 1.6.21 -> 2.2.10 | project::core -> com.github.doyaaaaaken:kotlin-csv -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-common | 1.8.0 -> 2.2.10 | project::core -> io.github.microutils:kotlin-logging-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-common | 1.9.10 -> 2.2.10 | org.jraf:klibnotion -> org.jraf:klibnotion-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-jdk7 | 1.8.0 -> 1.9.10 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-jdk7 | 1.9.10 | project::core -> com.github.doyaaaaaken:kotlin-csv -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-jdk8 | 1.8.0 -> 1.9.10 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ...<br>project::core -> com.linecorp.conditional:conditional-kotlin<br>project::core -> io.github.microutils:kotlin-logging-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-jdk8 | 1.6.21 -> 1.9.10 | project::core -> com.github.doyaaaaaken:kotlin-csv -> ... |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-jdk8 | 1.9.10 | org.jraf:klibnotion -> org.jraf:klibnotion-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-stdlib-jdk8 | 1.5.31 -> 1.9.10 | com.petersamokhin.notionsdk:notionsdk -> com.petersamokhin.notionsdk:notionsdk-jvm |
| 컴파일 | org.jetbrains.kotlin | kotlin-test | 2.1.10 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-collections-immutable | 0.4.0 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-collections-immutable-jvm | 0.4.0 | io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-bom | 1.10.2 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-core | {require 1.10.2; reject _} -> 1.10.2 | project::core |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-core | 1.10.2 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ...<br>aws.sdk.kotlin:s3 -> aws.sdk.kotlin:s3-jvm -> ...<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.ktor:ktor-server-core-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ...<br>io.ktor:ktor-client-okhttp-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-core | 1.8.1 -> 1.10.2 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-core | 1.10.1 -> 1.10.2 | io.ktor:ktor-server-netty -> io.ktor:ktor-server-netty-jvm<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm<br>io.ktor:ktor-server-html-builder-jvm<br>io.ktor:ktor-server-auth-jvm<br>io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions -> ...<br>io.ktor:ktor-server-auth-jwt-jvm<br>io.ktor:ktor-server-auto-head-response-jvm<br>io.ktor:ktor-server-host-common-jvm<br>io.ktor:ktor-server-status-pages-jvm<br>io.ktor:ktor-client-auth -> io.ktor:ktor-client-auth-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-core-jvm | 1.10.2 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core<br>project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-test | 1.10.2 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-test | 1.10.1 -> 1.10.2 | io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-coroutines-test-jvm | 1.10.2 | project::core -> org.jetbrains.kotlinx:kotlinx-coroutines-core -> ...<br>io.ktor:ktor-server-test-host -> io.ktor:ktor-server-test-host-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-html | 0.12.0 | io.ktor:ktor-server-html-builder-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-html-jvm | {require 0.12.0; reject _} -> 0.12.0 | project::core |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-html-jvm | 0.12.0 | io.ktor:ktor-server-html-builder-jvm -> org.jetbrains.kotlinx:kotlinx-html |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-io-bytestring | 0.8.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-io-bytestring-jvm | 0.8.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-io-core | 0.5.4 -> 0.8.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-io-core | 0.8.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-io-core-jvm | 0.8.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-bom | 1.9.0 | project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-core | 1.9.0 | project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json -> ...<br>com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-core | 1.8.0 -> 1.9.0 | io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-core | 1.7.3 -> 1.9.0 | com.charleskorn.kaml:kaml -> com.charleskorn.kaml:kaml-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-core-jvm | 1.9.0 | project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-json | {require 1.8.0; reject _} -> 1.9.0 | project::core |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-json | 1.9.0 | project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json -> ...<br>io.modelcontextprotocol:kotlin-sdk -> io.modelcontextprotocol:kotlin-sdk-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-json | 1.7.3 -> 1.9.0 | com.aallam.openai:openai-client -> com.aallam.openai:openai-client-jvm -> ... |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-json | 1.8.0 -> 1.9.0 | io.ktor:ktor-server-auth-jvm -> io.ktor:ktor-server-sessions -> ...<br>io.ktor:ktor-server-auth-jvm |
| 컴파일 | org.jetbrains.kotlinx | kotlinx-serialization-json-jvm | 1.9.0 | project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json<br>project::core -> org.jetbrains.kotlinx:kotlinx-serialization-json -> ... |

