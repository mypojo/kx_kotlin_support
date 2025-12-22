## JVM 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | jakarta.validation | jakarta.validation-api | {require 3.1.1; reject _} -> 3.1.1 |  |


## 사실상 표준

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | ch.qos.logback | logback-classic | {require 1.5.18; reject _} -> 1.5.18 |  |
| 컴파일/런타임 | ch.qos.logback | logback-core | 1.5.18 | ch.qos.logback:logback-classic |
| 컴파일/런타임 | org.slf4j | slf4j-api | 2.0.3 -> 2.0.17 | io.github.microutils:kotlin-logging-jvm |
| 컴파일/런타임 | org.slf4j | slf4j-api | 2.0.17 | ch.qos.logback:logback-classic |
| 런타임 | org.slf4j | slf4j-api | 2.0.11 -> 2.0.17 | com.jayway.jsonpath:json-path |


## 사실상 표준(코틀린)

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | com.github.doyaaaaaken | kotlin-csv | {require 1.10.0; reject _} -> 1.10.0 |  |
| 컴파일/런타임 | com.github.doyaaaaaken | kotlin-csv-jvm | 1.10.0 | com.github.doyaaaaaken:kotlin-csv |


## 사실상 표준의 의존성

| scope | 그룹 | 이름 | 버전 | 출처 |
|-------|------|------|------|------|
| 컴파일/런타임 | com.lectra | koson | {require 1.2.9; reject _} -> 1.2.9 |  |
