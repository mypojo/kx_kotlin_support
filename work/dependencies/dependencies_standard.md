## JVM 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | jakarta.activation | jakarta.activation-api | 1.2.1 -> 2.1.3 | org.apache.hadoop:hadoop-common |
| 런타임 | jakarta.annotation | jakarta.annotation-api | 2.1.1 | org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-client -> ...<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-server |
| 런타임 | jakarta.inject | jakarta.inject-api | 2.0.1 | org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-client -> ...<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-client<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-server<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.containers:jersey-container-servlet -> ... |
| 런타임 | jakarta.servlet | jakarta.servlet-api | 5.0.0 -> 6.0.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | jakarta.validation | jakarta.validation-api | 3.0.2 -> 3.1.1 | org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-server |
| 런타임 | jakarta.ws.rs | jakarta.ws.rs-api | 3.1.0 | org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-client<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-client -> ...<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.core:jersey-server<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.containers:jersey-container-servlet -> ...<br>org.apache.spark:spark-core_2.13 -> org.glassfish.jersey.containers:jersey-container-servlet |
| 런타임 | jakarta.xml.bind | jakarta.xml.bind-api | 4.0.2 | org.apache.hadoop:hadoop-common -> com.github.pjfanning:jersey-json -> ... |
| 런타임 | javax.servlet | javax.servlet-api | 4.0.1 | org.apache.spark:spark-core_2.13 |
| 런타임 | javax.servlet | javax.servlet-api | 3.1.0 -> 4.0.1 | org.apache.hadoop:hadoop-common |
| 런타임 | javax.servlet.jsp | jsp-api | 2.1 | org.apache.hadoop:hadoop-common |
| 런타임 | javax.ws.rs | jsr311-api | 1.1.1 | org.apache.hadoop:hadoop-common -> com.sun.jersey:jersey-core |


## 사실상 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | com.squareup.okhttp3 | okhttp | 4.12.0 -> 5.1.0 | ai.koog:koog-agents -> ai.koog:koog-agents-jvm -> ... |
| 런타임 | io.netty | netty-all | 4.1.118.Final -> 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 |
| 런타임 | io.netty | netty-buffer | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-codec | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-codec-dns | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-codec-http | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-codec-http2 | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-codec-socks | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-common | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-common | 4.1.118.Final -> 4.1.119.Final | ai.koog:koog-agents -> ai.koog:koog-agents-jvm -> ... |
| 런타임 | io.netty | netty-handler | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-handler | 4.1.118.Final -> 4.1.119.Final | ai.koog:koog-agents -> ai.koog:koog-agents-jvm -> ... |
| 런타임 | io.netty | netty-handler | 4.1.113.Final -> 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | io.netty | netty-handler-proxy | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-resolver | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-resolver-dns | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-tcnative-boringssl-static | 2.0.70.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 |
| 런타임 | io.netty | netty-tcnative-boringssl-static | 2.0.66.Final -> 2.0.70.Final | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | io.netty | netty-tcnative-classes | 2.0.70.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-transport | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-transport | 4.1.118.Final -> 4.1.119.Final | ai.koog:koog-agents -> ai.koog:koog-agents-jvm -> ... |
| 런타임 | io.netty | netty-transport-classes-epoll | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-transport-classes-kqueue | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-transport-native-epoll | 4.1.118.Final -> 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 |
| 런타임 | io.netty | netty-transport-native-epoll | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-transport-native-epoll | 4.1.113.Final -> 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | io.netty | netty-transport-native-kqueue | 4.1.118.Final -> 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 |
| 런타임 | io.netty | netty-transport-native-kqueue | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | io.netty | netty-transport-native-unix-common | 4.1.119.Final | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ... |
| 런타임 | joda-time | joda-time | 2.2 -> 2.10.10 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ... |
| 런타임 | org.junit | junit-bom | 5.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm -> ... |
| 런타임 | org.junit.jupiter | junit-jupiter-api | 5.8.2 -> 5.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm |
| 런타임 | org.junit.platform | junit-platform-commons | 1.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm -> ... |
| 런타임 | org.junit.platform | junit-platform-engine | 1.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm -> ... |
| 런타임 | org.junit.platform | junit-platform-engine | 1.8.2 -> 1.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm |
| 런타임 | org.junit.platform | junit-platform-launcher | 1.8.2 -> 1.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm |
| 런타임 | org.junit.platform | junit-platform-suite-api | 1.8.2 -> 1.12.0 | io.kotest:kotest-runner-junit5 -> io.kotest:kotest-runner-junit5-jvm |
| 런타임 | org.slf4j | jcl-over-slf4j | 2.0.16 -> 2.0.17 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.slf4j | jul-to-slf4j | 2.0.16 -> 2.0.17 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.slf4j | slf4j-api | 1.7.36 -> 2.0.17 | org.apache.spark:spark-core_2.13 -> org.apache.hadoop:hadoop-client-runtime<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 -> ...<br>org.apache.spark:spark-core_2.13 -> io.dropwizard.metrics:metrics-jvm<br>org.apache.spark:spark-core_2.13 -> io.dropwizard.metrics:metrics-graphite<br>org.apache.spark:spark-core_2.13 -> io.dropwizard.metrics:metrics-jmx<br>org.apache.hadoop:hadoop-common<br>org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |
| 런타임 | org.slf4j | slf4j-api | 2.0.16 -> 2.0.17 | ai.koog:koog-agents -> ai.koog:koog-agents-jvm -> ...<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ...<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13<br>org.apache.hadoop:hadoop-common -> org.eclipse.jetty:jetty-server -> ...<br>org.apache.hadoop:hadoop-common -> org.eclipse.jetty:jetty-server<br>org.apache.hadoop:hadoop-common -> org.eclipse.jetty:jetty-servlet -> ...<br>org.apache.hadoop:hadoop-common -> org.eclipse.jetty:jetty-webapp -> ... |
| 런타임 | org.slf4j | slf4j-api | 1.7.25 -> 2.0.17 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.slf4j | slf4j-api | 1.7.21 -> 2.0.17 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ...<br>org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-modelimport<br>org.deeplearning4j:deeplearning4j-core |
| 런타임 | org.slf4j | slf4j-api | 1.7.24 -> 2.0.17 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ...<br>org.deeplearning4j:deeplearning4j-core -> com.github.oshi:oshi-json |
| 런타임 | org.slf4j | slf4j-api | 1.7.10 -> 2.0.17 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ... |
| 런타임 | org.slf4j | slf4j-api | 2.0.13 -> 2.0.17 | org.apache.spark:spark-core_2.13 -> org.apache.avro:avro<br>org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred -> ...<br>org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred |
| 런타임 | org.slf4j | slf4j-api | 1.7.30 -> 2.0.17 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | org.slf4j | slf4j-api | 1.7.22 -> 2.0.17 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column -> ...<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop<br>org.apache.parquet:parquet-avro |


## 사실상 표준(코틀린)

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | io.insert-koin | koin-core | 3.5.0 -> 4.1.0-RC1 | io.kotest.extensions:kotest-extensions-koin -> io.kotest.extensions:kotest-extensions-koin-jvm<br>io.kotest.extensions:kotest-extensions-koin -> io.kotest.extensions:kotest-extensions-koin-jvm -> ... |
| 런타임 | io.insert-koin | koin-test | 3.5.0 | io.kotest.extensions:kotest-extensions-koin -> io.kotest.extensions:kotest-extensions-koin-jvm |
| 런타임 | io.insert-koin | koin-test-jvm | 3.5.0 | io.kotest.extensions:kotest-extensions-koin -> io.kotest.extensions:kotest-extensions-koin-jvm -> ... |


## 사실상 표준의 의존성

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | com.fasterxml.jackson.core | jackson-annotations | 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-annotations | 2.18.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13 |
| 런타임 | com.fasterxml.jackson.core | jackson-core | 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-core | 2.17.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.avro:avro<br>org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred -> ...<br>org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred |
| 런타임 | com.fasterxml.jackson.core | jackson-core | 2.18.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-unsafe_2.13 -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-core | 2.12.7 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> io.dropwizard.metrics:metrics-json |
| 런타임 | com.fasterxml.jackson.core | jackson-databind | 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-databind | 2.17.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.avro:avro<br>org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred -> ... |
| 런타임 | com.fasterxml.jackson.core | jackson-databind | 2.18.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ...<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 |
| 런타임 | com.fasterxml.jackson.core | jackson-databind | 2.12.7.1 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> io.jsonwebtoken:jjwt-jackson<br>org.apache.hadoop:hadoop-common |
| 런타임 | com.fasterxml.jackson.core | jackson-databind | 2.12.7.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> io.dropwizard.metrics:metrics-json |
| 런타임 | com.fasterxml.jackson.module | jackson-module-scala_2.13 | 2.18.2 -> 2.18.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ...<br>org.apache.spark:spark-core_2.13 |
| 런타임 | commons-codec | commons-codec | 1.17.2 | org.apache.spark:spark-core_2.13 |
| 런타임 | commons-codec | commons-codec | 1.10 -> 1.17.2 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ... |
| 런타임 | commons-codec | commons-codec | 1.15 -> 1.17.2 | org.apache.hadoop:hadoop-common<br>org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |


## 아파치/커먼스 시리즈

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 런타임 | commons-beanutils | commons-beanutils | 1.9.4 -> 1.11.0 | org.apache.hadoop:hadoop-common |
| 런타임 | commons-cli | commons-cli | 1.2 | org.apache.hadoop:hadoop-common |
| 런타임 | commons-codec | commons-codec | 1.17.2 | org.apache.spark:spark-core_2.13 |
| 런타임 | commons-codec | commons-codec | 1.10 -> 1.17.2 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ... |
| 런타임 | commons-codec | commons-codec | 1.15 -> 1.17.2 | org.apache.hadoop:hadoop-common<br>org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |
| 런타임 | commons-collections | commons-collections | 3.2.2 | org.apache.spark:spark-core_2.13<br>org.apache.hadoop:hadoop-common |
| 런타임 | commons-io | commons-io | 2.18.0 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ...<br>org.apache.spark:spark-core_2.13 |
| 런타임 | commons-io | commons-io | 2.17.0 -> 2.18.0 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | commons-io | commons-io | 2.7 -> 2.18.0 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ...<br>org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-modelimport -> ...<br>org.deeplearning4j:deeplearning4j-core<br>org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-ui-components |
| 런타임 | commons-io | commons-io | 2.8.0 -> 2.18.0 | org.apache.hadoop:hadoop-common |
| 런타임 | commons-io | commons-io | 2.5 -> 2.18.0 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | commons-lang | commons-lang | 2.6 | org.deeplearning4j:deeplearning4j-nlp |
| 런타임 | commons-net | commons-net | 3.1 -> 3.9.0 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ... |
| 런타임 | commons-net | commons-net | 3.9.0 | org.apache.hadoop:hadoop-common |
| 런타임 | commons-pool | commons-pool | 1.6 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop |
| 런타임 | org.apache.avro | avro | 1.12.0 | org.apache.spark:spark-core_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred -> ... |
| 런타임 | org.apache.avro | avro | 1.11.1 -> 1.12.0 | org.apache.parquet:parquet-avro |
| 런타임 | org.apache.avro | avro | 1.7.7 -> 1.12.0 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.avro | avro-ipc | 1.12.0 | org.apache.spark:spark-core_2.13 -> org.apache.avro:avro-mapred |
| 런타임 | org.apache.avro | avro-mapred | 1.12.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-collections4 | 4.4 -> 4.5.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-collections4 | 4.1 -> 4.5.0 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ... |
| 런타임 | org.apache.commons | commons-compress | 1.27.1 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-compress | 1.21 -> 1.27.1 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ...<br>org.deeplearning4j:deeplearning4j-core<br>org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.commons | commons-compress | 1.26.2 -> 1.27.1 | org.apache.spark:spark-core_2.13 -> org.apache.avro:avro |
| 런타임 | org.apache.commons | commons-configuration2 | 2.8.0 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.commons | commons-crypto | 1.1.0 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-lang3 | 3.17.0 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-lang3 | 3.11 -> 3.17.0 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ...<br>org.deeplearning4j:deeplearning4j-core<br>org.deeplearning4j:deeplearning4j-nlp |
| 런타임 | org.apache.commons | commons-lang3 | 3.12.0 -> 3.17.0 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.commons | commons-math3 | 3.6.1 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-math3 | 3.5 -> 3.6.1 | org.deeplearning4j:deeplearning4j-core -> org.deeplearning4j:deeplearning4j-datasets -> ...<br>org.deeplearning4j:deeplearning4j-core |
| 런타임 | org.apache.commons | commons-math3 | 3.1.1 -> 3.6.1 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.commons | commons-text | 1.13.0 -> 1.13.1 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ...<br>org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.commons | commons-text | 1.9 -> 1.13.1 | org.apache.hadoop:hadoop-common -> org.apache.commons:commons-configuration2 |
| 런타임 | org.apache.commons | commons-text | 1.10.0 -> 1.13.1 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.curator | curator-client | 5.7.1 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | org.apache.curator | curator-client | 5.2.0 -> 5.7.1 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.curator | curator-framework | 5.7.1 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes |
| 런타임 | org.apache.curator | curator-framework | 5.2.0 -> 5.7.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |
| 런타임 | org.apache.curator | curator-recipes | 5.7.1 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.curator | curator-recipes | 5.2.0 -> 5.7.1 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.hadoop | hadoop-annotations | 3.3.6 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.hadoop | hadoop-auth | 3.3.6 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.hadoop | hadoop-client-api | 3.4.1 | org.apache.spark:spark-core_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.hadoop:hadoop-client-runtime |
| 런타임 | org.apache.hadoop | hadoop-client-runtime | 3.4.1 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.hadoop | hadoop-common | 3.3.6 |  |
| 런타임 | org.apache.hadoop.thirdparty | hadoop-shaded-guava | 1.1.1 | org.apache.hadoop:hadoop-common<br>org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |
| 런타임 | org.apache.hadoop.thirdparty | hadoop-shaded-protobuf_3_7 | 1.1.1 | org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.httpcomponents | httpclient | 4.5.13 -> 4.5.14 | org.apache.hadoop:hadoop-common<br>org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |
| 런타임 | org.apache.httpcomponents | httpclient | 4.5.14 | io.delta:delta-sharing-spark_2.13 -> io.delta:delta-sharing-client_2.13 |
| 런타임 | org.apache.ivy | ivy | 2.5.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.apache.kerby | kerb-admin | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerb-client | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerb-common | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerb-core | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ...<br>org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.kerby | kerb-crypto | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerb-identity | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerb-server | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerb-simplekdc | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth |
| 런타임 | org.apache.kerby | kerb-util | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerby-asn1 | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerby-config | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerby-pkix | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerby-util | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | kerby-xdr | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.kerby | token-provider | 1.0.1 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth -> ... |
| 런타임 | org.apache.logging.log4j | log4j-1.2-api | 2.24.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.apache.logging.log4j | log4j-api | 2.24.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.apache.logging.log4j | log4j-core | 2.24.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.apache.logging.log4j | log4j-layout-template-json | 2.24.3 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.apache.parquet | parquet-avro | 1.13.1 |  |
| 런타임 | org.apache.parquet | parquet-column | 1.13.1 | org.apache.parquet:parquet-avro<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop |
| 런타임 | org.apache.parquet | parquet-common | 1.13.1 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column -> ...<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop<br>org.apache.parquet:parquet-avro |
| 런타임 | org.apache.parquet | parquet-encoding | 1.13.1 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column |
| 런타임 | org.apache.parquet | parquet-format-structures | 1.13.1 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column -> ...<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop |
| 런타임 | org.apache.parquet | parquet-hadoop | 1.13.1 | org.apache.parquet:parquet-avro |
| 런타임 | org.apache.parquet | parquet-jackson | 1.13.1 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop |
| 런타임 | org.apache.spark | spark-common-utils_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-common_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-unsafe_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-unsafe_2.13 -> ...<br>org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.spark | spark-core_2.13 | 4.0.0 |  |
| 런타임 | org.apache.spark | spark-kvstore_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.spark | spark-launcher_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.spark | spark-network-common_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-shuffle_2.13 |
| 런타임 | org.apache.spark | spark-network-shuffle_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.spark | spark-tags_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-launcher_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ...<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-network-shuffle_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-unsafe_2.13<br>org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-unsafe_2.13 -> ...<br>org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.spark | spark-unsafe_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.spark | spark-variant_2.13 | 4.0.0 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-unsafe_2.13 |
| 런타임 | org.apache.xbean | xbean-asm9-shaded | 4.26 | org.apache.spark:spark-core_2.13 -> org.apache.spark:spark-kvstore_2.13 -> ... |
| 런타임 | org.apache.yetus | audience-annotations | 0.12.0 -> 0.13.0 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | org.apache.yetus | audience-annotations | 0.13.0 | org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-column<br>org.apache.parquet:parquet-avro -> org.apache.parquet:parquet-hadoop |
| 런타임 | org.apache.zookeeper | zookeeper | 3.9.2 -> 3.9.3 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
| 런타임 | org.apache.zookeeper | zookeeper | 3.9.3 | org.apache.spark:spark-core_2.13 |
| 런타임 | org.apache.zookeeper | zookeeper | 3.6.3 -> 3.9.3 | org.apache.hadoop:hadoop-common -> org.apache.hadoop:hadoop-auth<br>org.apache.hadoop:hadoop-common |
| 런타임 | org.apache.zookeeper | zookeeper-jute | 3.9.3 | org.apache.spark:spark-core_2.13 -> org.apache.curator:curator-recipes -> ... |
