# CLAUDE.md

- This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
- 상세 코드 가이드라인은 .junie/guidelines.md 파일을 를 참고해줘
- 처리가 완료되면 슬렉으로 간단하게 완료 메세지를 전달해줘 
  - 채널 이름 = 'ai-콜백'
- 테스트 코드는 작성만 해줘. 실행해볼 필요는 없어
- 모든 결과나 프로세스 과정 등의 텍스트는 가능하면 한글을 사용해줘

## Architecture Overview
This is a multi-module Kotlin project that provides utility libraries for AWS services, Spring Boot applications, and general Kotlin development. The project is structured as follows:

### Module Structure
- **core**: Minimal dependencies utility module with basic Kotlin extensions, collection utilities, time handling, CSV processing, and validation
- **light**: AWS Lambda-focused module extending core with AWS SDK utilities (DynamoDB, S3, SQS, etc.), external API integrations (Google, Slack, GitHub), and optimized for cold start performance
- **heavy**: Spring Boot module with JPA/Hibernate support, batch processing, security, and comprehensive web application features
- **ksp**: Kotlin Symbol Processing annotation processors
- **work**: Local development and testing utilities with notebook support

### Key Technologies
- **Kotlin**: Primary language with coroutines, serialization, and multiplatform support
- **Gradle**: Build system using Kotlin DSL with version catalog management via refreshVersions
- **Spring Boot**: Web framework (heavy module only)
- **AWS SDK**: Comprehensive AWS service integration
- **Kotest**: Testing framework with snapshot versions
- **Koin**: Dependency injection

### Key Patterns
- **DSL-heavy code**: Extensive use of Kotlin DSL for AWS CDK, configuration builders, and domain-specific languages
- **Flow-based processing**: Heavy use of Kotlin Flow for data streaming and CSV processing
- **Resource management**: Custom resource handling with lazy loading and AWS S3 integration
- **Extension functions**: Extensive use of extension functions for enhancing existing APIs
- **Coroutine-first**: Async programming with structured concurrency patterns

### AWS Integration
The project provides extensive AWS service wrappers including:
- **Kinesis**: Real-time data processing with worker/task patterns
- **CDK**: Infrastructure as code with Kotlin DSL
- **ECS**: Blue-green deployment support
- **Step Functions**: State machine orchestration
- **Lambda**: Optimized cold start handling
- **DynamoDB**: Enhanced query support and session management