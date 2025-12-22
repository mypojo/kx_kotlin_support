## JVM 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | jakarta.activation | jakarta.activation-api | 2.1.3 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core -> ... |
| 컴파일/런타임 | jakarta.annotation | jakarta.annotation-api | 2.1.1 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-tomcat |
| 컴파일/런타임 | jakarta.annotation | jakarta.annotation-api | 2.0.0 -> 2.1.1 | org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa |
| 런타임 | jakarta.inject | jakarta.inject-api | 2.0.1 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 컴파일/런타임 | jakarta.persistence | jakarta.persistence-api | 3.1.0 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 컴파일/런타임 | jakarta.transaction | jakarta.transaction-api | 2.0.1 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 컴파일/런타임 | jakarta.validation | jakarta.validation-api | 3.0.2 -> 3.1.1 | org.springframework.boot:spring-boot-dependencies<br>org.hibernate.validator:hibernate-validator -> 8.0.2.Final |
| 런타임 | jakarta.xml.bind | jakarta.xml.bind-api | 4.0.2 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core -> ... |
| 런타임 | jakarta.xml.bind | jakarta.xml.bind-api | 4.0.0 -> 4.0.2 | org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 컴파일/런타임 | javax.annotation | javax.annotation-api | [1.3.2,1.4.0) -> 1.3.2 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ...<br>software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1<br>software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-node-proxy-agent-v6<br>software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-cloud-assembly-schema<br>software.amazon.awscdk:aws-cdk-lib -> software.constructs:constructs<br>software.amazon.awscdk:aws-cdk-lib |


## 사실상 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | ch.qos.logback | logback-classic | 1.5.18 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-starter-logging |
| 컴파일/런타임 | ch.qos.logback | logback-core | 1.5.18 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | com.mysql | mysql-connector-j | {require 9.2.0; reject _} -> 9.2.0 |  |
| 컴파일/런타임 | com.mysql | mysql-connector-j | 9.1.0 -> 9.2.0 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | com.zaxxer | HikariCP | {require 5.1.0; reject _} -> 5.1.0 |  |
| 컴파일/런타임 | com.zaxxer | HikariCP | 3.1.0 -> 5.1.0 | com.vladsch.kotlin-jdbc:kotlin-jdbc |
| 컴파일/런타임 | com.zaxxer | HikariCP | 5.1.0 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.boot:spring-boot-starter-jdbc |
| 컴파일 | com.zaxxer | SparseBitSet | 1.3 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
| 컴파일 | io.netty | netty-buffer | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-codec | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-codec-http | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | io.netty | netty-codec-http2 | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-common | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-handler | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-resolver | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-transport | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | io.netty | netty-transport-classes-epoll | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | io.netty | netty-transport-classes-kqueue | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | io.netty | netty-transport-native-epoll | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | io.netty | netty-transport-native-kqueue | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | io.netty | netty-transport-native-unix-common | 4.1.119.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | io.swagger.core.v3 | swagger-annotations-jakarta | {require 2.2.34; reject _} -> 2.2.34 |  |
| 컴파일/런타임 | joda-time | joda-time | 2.10.10 | org.jetbrains.exposed:exposed |
| 컴파일/런타임 | joda-time | joda-time | 2.9.9 -> 2.10.10 | com.vladsch.kotlin-jdbc:kotlin-jdbc |
| 컴파일/런타임 | junit | junit | 4.13.2 | com.opencsv:opencsv -> org.junit.vintage:junit-vintage-engine<br>org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.junit | junit-bom | 5.12.0 | com.opencsv:opencsv -> org.junit.vintage:junit-vintage-engine<br>com.opencsv:opencsv -> org.junit.vintage:junit-vintage-engine -> ... |
| 컴파일/런타임 | org.junit.jupiter | junit-jupiter | 5.11.4 -> 5.12.0 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.junit.jupiter | junit-jupiter-api | 5.11.4 -> 5.12.0 | org.springframework.boot:spring-boot-dependencies |
| 런타임 | org.junit.jupiter | junit-jupiter-engine | 5.11.4 -> 5.12.0 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.junit.jupiter | junit-jupiter-params | 5.11.4 -> 5.12.0 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.junit.platform | junit-platform-commons | 1.12.0 | com.opencsv:opencsv -> org.junit.vintage:junit-vintage-engine -> ... |
| 컴파일/런타임 | org.junit.platform | junit-platform-commons | 1.11.4 -> 1.12.0 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.junit.platform | junit-platform-engine | 1.12.0 | com.opencsv:opencsv -> org.junit.vintage:junit-vintage-engine |
| 컴파일/런타임 | org.junit.platform | junit-platform-engine | 1.11.4 -> 1.12.0 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.junit.vintage | junit-vintage-engine | 5.11.4 -> 5.12.0 | com.opencsv:opencsv<br>org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.postgresql | postgresql | {require 42.7.8; reject _} -> 42.7.8 |  |
| 컴파일/런타임 | org.postgresql | postgresql | 42.7.5 -> 42.7.8 | org.springframework.boot:spring-boot-dependencies |
| 런타임 | org.slf4j | jcl-over-slf4j | 2.0.7 -> 2.0.17 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-jdbc -> ... |
| 런타임 | org.slf4j | jcl-over-slf4j | 2.0.17 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.slf4j | jul-to-slf4j | 2.0.17 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-starter-logging |
| 컴파일/런타임 | org.slf4j | slf4j-api | 2.0.17 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-starter-logging -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-jdbc -> ... |
| 컴파일/런타임 | org.slf4j | slf4j-api | 1.7.36 -> 2.0.17 | com.zaxxer:HikariCP |
| 컴파일/런타임 | org.slf4j | slf4j-api | 2.0.16 -> 2.0.17 | org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-starter-logging -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-core<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-csv |
| 컴파일/런타임 | org.slf4j | slf4j-api | 1.7.25 -> 2.0.17 | org.jetbrains.exposed:exposed<br>com.vladsch.kotlin-jdbc:kotlin-jdbc |
| 컴파일/런타임 | org.slf4j | slf4j-api | 2.0.2 -> 2.0.17 | org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa |
| 런타임 | org.slf4j | slf4j-api | 2.0.7 -> 2.0.17 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-jdbc -> ... |


## 사실상 표준(스프링/하이버네이트)

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | org.hibernate.common | hibernate-commons-annotations | 7.0.3.Final | org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 컴파일/런타임 | org.hibernate.orm | hibernate-core | 6.6.13.Final | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 |
| 컴파일/런타임 | org.hibernate.validator | hibernate-validator | 8.0.2.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.hibernate.validator | hibernate-validator -> 8.0.2.Final |  |  |
| 컴파일/런타임 | org.jboss.logging | jboss-logging | 3.6.1.Final | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.jboss.logging | jboss-logging | 3.4.3.Final -> 3.6.1.Final | org.hibernate.validator:hibernate-validator -> 8.0.2.Final |
| 런타임 | org.jboss.logging | jboss-logging | 3.5.0.Final -> 3.6.1.Final | org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 컴파일/런타임 | org.springframework | spring-aop | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot -> ...<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework:spring-webmvc<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config -> ...<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web |
| 컴파일/런타임 | org.springframework | spring-aop | 6.2.4 -> 6.2.6 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework | spring-aspects | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 |
| 컴파일/런타임 | org.springframework | spring-beans | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot -> ...<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json -> ...<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework:spring-webmvc<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.boot:spring-boot-starter-jdbc -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config -> ...<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web |
| 컴파일/런타임 | org.springframework | spring-beans | 6.2.4 -> 6.2.6 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework | spring-context | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework:spring-webmvc<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config -> ...<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web |
| 컴파일/런타임 | org.springframework | spring-context | 6.2.4 -> 6.2.6 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework | spring-core | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot -> ...<br>org.springframework.boot:spring-boot-starter -> 3.4.5<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json -> ...<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework:spring-webmvc<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.boot:spring-boot-starter-jdbc -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config -> ...<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server -> ...<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-jose |
| 컴파일/런타임 | org.springframework | spring-core | 6.2.4 -> 6.2.6 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core -> ... |
| 컴파일/런타임 | org.springframework | spring-expression | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot -> ...<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework:spring-webmvc<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config -> ...<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web |
| 컴파일/런타임 | org.springframework | spring-jcl | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot -> ... |
| 컴파일/런타임 | org.springframework | spring-jcl | 6.2.5 -> 6.2.6 | org.springframework.session:spring-session-core -> 3.4.3 |
| 컴파일/런타임 | org.springframework | spring-jdbc | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.boot:spring-boot-starter-jdbc<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa -> ... |
| 컴파일/런타임 | org.springframework | spring-jdbc | 6.2.4 -> 6.2.6 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework | spring-orm | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa |
| 컴파일/런타임 | org.springframework | spring-tx | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.boot:spring-boot-starter-jdbc -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa -> ...<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa |
| 컴파일/런타임 | org.springframework | spring-tx | 6.2.4 -> 6.2.6 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework | spring-web | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework:spring-webmvc<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server -> ... |
| 컴파일/런타임 | org.springframework | spring-webmvc | 6.2.6 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 |
| 컴파일/런타임 | org.springframework.batch | spring-batch-core | 5.2.2 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-batch -> 3.4.5 |
| 컴파일/런타임 | org.springframework.batch | spring-batch-infrastructure | 5.2.2 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework.boot | spring-boot | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-autoconfigure |
| 컴파일/런타임 | org.springframework.boot | spring-boot-autoconfigure | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 |
| 컴파일/런타임 | org.springframework.boot | spring-boot-dependencies | 3.4.5 |  |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.boot:spring-boot-starter-jdbc<br>org.springframework.boot:spring-boot-starter-batch -> 3.4.5<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter -> 3.4.5 |  |  |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-batch | 3.4.5 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-batch -> 3.4.5 |  |  |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-data-jpa | 3.4.5 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-data-jpa -> 3.4.5 |  |  |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-jdbc | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5<br>org.springframework.boot:spring-boot-starter-batch -> 3.4.5 |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-json | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-logging | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-oauth2-resource-server | 3.4.5 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-oauth2-resource-server -> 3.4.5 |  |  |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-security | 3.4.5 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-security -> 3.4.5 |  |  |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-tomcat | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-web | 3.4.5 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.springframework.boot | spring-boot-starter-web -> 3.4.5 |  |  |
| 컴파일/런타임 | org.springframework.data | spring-data-commons | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.springframework.data:spring-data-jpa |
| 컴파일/런타임 | org.springframework.data | spring-data-commons | 3.4.4 -> 3.4.5 | org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core |
| 컴파일/런타임 | org.springframework.data | spring-data-jpa | 3.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 |
| 컴파일/런타임 | org.springframework.retry | spring-retry | 2.0.11 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-batch -> 3.4.5 -> org.springframework.batch:spring-batch-core -> ... |
| 컴파일/런타임 | org.springframework.retry | spring-retry -> 2.0.11 |  |  |
| 컴파일/런타임 | org.springframework.security | spring-security-config | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 |
| 컴파일/런타임 | org.springframework.security | spring-security-core | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-web<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server -> ...<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-jose |
| 컴파일/런타임 | org.springframework.security | spring-security-crypto | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5 -> org.springframework.security:spring-security-config -> ... |
| 컴파일/런타임 | org.springframework.security | spring-security-oauth2-core | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-jose |
| 컴파일/런타임 | org.springframework.security | spring-security-oauth2-jose | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 |
| 컴파일/런타임 | org.springframework.security | spring-security-oauth2-resource-server | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 |
| 컴파일/런타임 | org.springframework.security | spring-security-web | 6.4.5 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-security -> 3.4.5<br>org.springframework.boot:spring-boot-starter-oauth2-resource-server -> 3.4.5 -> org.springframework.security:spring-security-oauth2-resource-server |
| 컴파일/런타임 | org.springframework.session | spring-session-core | 3.4.3 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.springframework.session | spring-session-core -> 3.4.3 |  |  |


## 사실상 표준의 의존성

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | com.fasterxml.jackson | jackson-bom | 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ...<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.core | jackson-annotations | 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ...<br>org.springframework.boot:spring-boot-dependencies<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-annotations | 2.18.0 -> 2.18.3 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.core | jackson-core | 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ...<br>org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.core | jackson-core | [2.11.3,2.99999.0] -> 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.core | jackson-core | 2.15.0 -> 2.18.3 | com.dropbox.core:dropbox-core-sdk |
| 런타임 | com.fasterxml.jackson.core | jackson-core | 2.18.0 -> 2.18.3 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.core | jackson-databind | 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ...<br>org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.core | jackson-databind | [2.11.3,2.99999.0] -> 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-databind | 2.18.0 -> 2.18.3 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.datatype | jackson-datatype-jdk8 | 2.18.3 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json |
| 컴파일/런타임 | com.fasterxml.jackson.datatype | jackson-datatype-jsr310 | 2.18.3 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json |
| 컴파일/런타임 | com.fasterxml.jackson.datatype | jackson-datatype-jsr310 | [2.11.3,2.99999.0] -> 2.18.3 | software.amazon.awscdk:aws-cdk-lib -> software.amazon.awscdk:cdk-asset-awscli-v1 -> ... |
| 런타임 | com.fasterxml.jackson.datatype | jackson-datatype-jsr310 | 2.18.0 -> 2.18.3 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | com.fasterxml.jackson.module | jackson-module-parameter-names | 2.18.3 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-json |
| 컴파일/런타임 | commons-codec | commons-codec | 1.17.1 -> 1.17.2 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-core -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ...<br>org.apache.poi:poi-ooxml -> org.apache.commons:commons-compress<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | commons-codec | commons-codec | 1.17.2 | org.springframework.boot:spring-boot-dependencies |
| 런타임 | net.bytebuddy | byte-buddy | 1.15.11 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-data-jpa -> 3.4.5 -> org.hibernate.orm:hibernate-core |
| 런타임 | net.bytebuddy | byte-buddy-agent | 1.15.11 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.yaml | snakeyaml | 2.3 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 |


## 아파치/커먼스 시리즈

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | commons-beanutils | commons-beanutils | 1.11.0 | com.opencsv:opencsv |
| 컴파일/런타임 | commons-codec | commons-codec | 1.17.1 -> 1.17.2 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-core -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ...<br>org.apache.poi:poi-ooxml -> org.apache.commons:commons-compress<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일/런타임 | commons-codec | commons-codec | 1.17.2 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | commons-collections | commons-collections | 3.2.2 | com.opencsv:opencsv -> commons-beanutils:commons-beanutils |
| 컴파일/런타임 | commons-io | commons-io | 2.17.0 -> 2.18.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-core -> ... |
| 컴파일 | commons-io | commons-io | 2.18.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ...<br>org.apache.poi:poi-ooxml<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-core<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-csv |
| 컴파일 | commons-io | commons-io | 2.16.1 -> 2.18.0 | org.apache.poi:poi-ooxml -> org.apache.commons:commons-compress<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 런타임 | org.apache.arrow | arrow-format | 18.1.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow |
| 런타임 | org.apache.arrow | arrow-memory-core | 18.1.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 런타임 | org.apache.arrow | arrow-memory-unsafe | 18.1.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow |
| 런타임 | org.apache.arrow | arrow-vector | 18.1.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow |
| 컴파일 | org.apache.commons | commons-collections4 | 4.4 -> 4.5.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ...<br>org.apache.poi:poi-ooxml |
| 컴파일/런타임 | org.apache.commons | commons-collections4 | 4.5.0 | com.opencsv:opencsv |
| 컴파일 | org.apache.commons | commons-compress | 1.27.1 | org.apache.poi:poi-ooxml<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
| 컴파일/런타임 | org.apache.commons | commons-csv | 1.12.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-core<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-csv |
| 컴파일/런타임 | org.apache.commons | commons-lang3 | 3.17.0 | com.opencsv:opencsv<br>org.springframework.boot:spring-boot-dependencies |
| 컴파일 | org.apache.commons | commons-lang3 | 3.16.0 -> 3.17.0 | org.apache.poi:poi-ooxml -> org.apache.commons:commons-compress<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-arrow -> ... |
| 컴파일 | org.apache.commons | commons-math3 | 3.6.1 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
| 컴파일/런타임 | org.apache.commons | commons-text | 1.13.1 | com.opencsv:opencsv |
| 컴파일/런타임 | org.apache.httpcomponents | httpasyncclient | 4.1.5 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.apache.httpcomponents | httpcore | 4.4.16 | org.springframework.boot:spring-boot-dependencies |
| 컴파일/런타임 | org.apache.httpcomponents | httpcore-nio | 4.4.16 | org.springframework.boot:spring-boot-dependencies |
| 컴파일 | org.apache.logging.log4j | log4j-api | 2.24.3 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ...<br>org.apache.poi:poi-ooxml<br>org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-starter-logging -> ... |
| 컴파일 | org.apache.logging.log4j | log4j-api | 2.24.2 -> 2.24.3 | org.apache.poi:poi-ooxml -> org.apache.poi:poi-ooxml-lite -> ...<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
| 컴파일/런타임 | org.apache.logging.log4j | log4j-to-slf4j | 2.24.3 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter -> 3.4.5 -> org.springframework.boot:spring-boot-starter-logging |
| 컴파일/런타임 | org.apache.poi | poi | 5.3.0 -> 5.4.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel |
| 컴파일 | org.apache.poi | poi | 5.4.0 | org.apache.poi:poi-ooxml<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
| 컴파일/런타임 | org.apache.poi | poi-ooxml | {require 5.4.0; reject _} -> 5.4.0 |  |
| 런타임 | org.apache.poi | poi-ooxml | 5.3.0 -> 5.4.0 | org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel |
| 컴파일 | org.apache.poi | poi-ooxml-lite | 5.4.0 | org.apache.poi:poi-ooxml<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
| 컴파일/런타임 | org.apache.tomcat.embed | tomcat-embed-core | 10.1.40 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-tomcat<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-tomcat -> ... |
| 컴파일/런타임 | org.apache.tomcat.embed | tomcat-embed-el | 10.1.40 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-tomcat |
| 컴파일/런타임 | org.apache.tomcat.embed | tomcat-embed-websocket | 10.1.40 | org.springframework.boot:spring-boot-dependencies<br>org.springframework.boot:spring-boot-starter-web -> 3.4.5 -> org.springframework.boot:spring-boot-starter-tomcat |
| 컴파일 | org.apache.xmlbeans | xmlbeans | 5.3.0 | org.apache.poi:poi-ooxml -> org.apache.poi:poi-ooxml-lite<br>org.apache.poi:poi-ooxml<br>org.jetbrains.kotlinx:dataframe -> org.jetbrains.kotlinx:dataframe-excel -> ... |
