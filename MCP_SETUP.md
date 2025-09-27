# 클로드 데스크탑에서 Kotlin MCP 서버 설정하기

## 개요
이 문서는 공식 Kotlin MCP SDK를 사용해서 작성된 날짜 서버를 클로드 데스크탑에 연결하는 방법을 설명합니다.

## 필요한 것들
- 클로드 데스크탑 애플리케이션
- Java/Kotlin 실행 환경 (JDK 17 이상)
- 이 프로젝트의 JAR 파일

## 1. JAR 파일 빌드

프로젝트 루트에서 다음 명령을 실행하여 JAR 파일을 생성합니다:

```bash
./gradlew :light:build
```

빌드가 완료되면 `light/build/libs/` 디렉토리에 JAR 파일이 생성됩니다.

## 2. MCP 서버 구현

이 프로젝트는 공식 Kotlin MCP SDK (`io.modelcontextprotocol:kotlin-sdk`)를 사용하여 구현되었습니다:

- `McpDateServer`: 공식 SDK의 `Server` 클래스를 사용한 MCP 서버
- `McpServerMain`: 서버 실행을 위한 메인 함수
- `getCurrentDate` 도구: 현재 날짜와 시간을 다양한 형식으로 제공

### 주요 기능
- 현재 날짜 조회 (기본 형식: yyyy-MM-dd)
- 사용자 지정 날짜 형식 지원
- 현재 날짜와 시간 정보 제공
- 오류 처리 및 검증

## 3. 클로드 데스크탑 설정

### 3.1 설정 파일 위치
클로드 데스크탑의 설정 파일은 다음 위치에 있습니다:

- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

### 3.2 설정 파일 편집

설정 파일을 열고 다음과 같이 MCP 서버를 추가합니다:

```json
{
  "mcpServers": {
    "kotlin-date-server": {
      "command": "java",
      "args": [
        "-jar",
        "C:/Users/NHN/IdeaProjects/kx_kotlin_support/light/build/libs/light.jar",
        "net.kotlinx.ai.mcp.McpServerMainKt"
      ]
    }
  }
}
```

**주의사항**:
- JAR 파일의 경로는 실제 빌드된 파일 경로로 변경해야 합니다
- Windows에서는 백슬래시(`\\`) 대신 슬래시(`/`)를 사용하거나 백슬래시를 두 번 사용합니다 (`\\\\`)
- 공식 SDK는 코루틴을 사용하므로 JVM에서 적절한 런타임 설정이 필요할 수 있습니다

### 3.3 개발 환경에서의 실행 방법

개발 중에는 Gradle을 통해 직접 실행할 수도 있습니다:

```json
{
  "mcpServers": {
    "kotlin-date-server": {
      "command": "cmd",
      "args": [
        "/c",
        "cd /d C:\\Users\\NHN\\IdeaProjects\\kx_kotlin_support && .\\gradlew.bat :light:run -PmainClass=net.kotlinx.ai.mcp.McpServerMainKt"
      ]
    }
  }
}
```

또는 Unix/Linux/macOS 환경에서는:

```json
{
  "mcpServers": {
    "kotlin-date-server": {
      "command": "./gradlew",
      "args": [
        ":light:run",
        "-PmainClass=net.kotlinx.ai.mcp.McpServerMainKt"
      ],
      "cwd": "/path/to/kx_kotlin_support"
    }
  }
}
```

## 4. 클로드 데스크탑 재시작

설정을 변경한 후 클로드 데스크탑을 완전히 종료하고 다시 시작합니다.

## 5. 사용법

클로드 데스크탑에서 다음과 같이 날짜 관련 질문을 할 수 있습니다:

- "오늘 날짜를 알려줘"
- "현재 날짜와 시간을 알려줘"
- "날짜를 'yyyy년 MM월 dd일' 형식으로 보여줘"
- "getCurrentDate 도구를 사용해서 현재 날짜를 조회해줘"

MCP 서버는 `getCurrentDate`라는 도구를 제공하며, 선택적으로 `format` 매개변수를 받습니다.

## 6. 문제 해결

### MCP 서버가 연결되지 않는 경우:
1. **JAR 파일 경로 확인**: 빌드된 JAR 파일의 실제 경로가 설정과 일치하는지 확인
2. **Java 설치 확인**: Java 17 이상이 설치되어 있고 시스템 PATH에 포함되어 있는지 확인
3. **의존성 확인**: MCP SDK와 필요한 Ktor 엔진이 포함된 JAR인지 확인
4. **클로드 데스크탑 개발자 도구**: 에러 메시지 확인

### 개발자 도구 열기:
- Windows/Linux: `Ctrl + Shift + I`
- macOS: `Cmd + Option + I`

### 로그 확인:
개발자 도구의 콘솔 탭에서 MCP 관련 오류 메시지를 확인할 수 있습니다.

### 일반적인 오류들:
- **"Server process exited"**: JAR 파일 경로가 잘못되었거나 Java가 설치되지 않았을 때
- **"Connection timeout"**: 서버 시작에 시간이 오래 걸리거나 코루틴 설정 문제
- **"Tool not found"**: 도구 이름이 잘못되었을 때

## 7. 기능 확장

더 많은 기능을 추가하려면 `McpDateServer.kt` 파일의 `setupTools()` 메서드에서 `server.addTool()`을 사용하여 새로운 도구를 추가할 수 있습니다:

```kotlin
server.addTool(
    name = "getTimeZone",
    description = "현재 시간대 정보를 가져옵니다",
    inputSchema = mapOf("type" to "object")
) { request ->
    val timeZone = TimeZone.getDefault()
    CallToolResult(
        content = listOf(
            TextContent(
                type = "text",
                text = "현재 시간대: ${timeZone.displayName}"
            )
        ),
        isError = false
    )
}
```

## 8. 예시 응답

MCP 서버가 정상적으로 작동하면 다음과 같은 응답을 받을 수 있습니다:

### 기본 형식:
```json
{
    "today": "2024-09-24",
    "todayFormatted": "2024-09-24",
    "currentDateTime": "2024-09-24T15:30:45.123456"
}
```

### 사용자 지정 형식 (yyyy년 MM월 dd일):
```json
{
    "today": "2024-09-24",
    "todayFormatted": "2024년 09월 24일",
    "currentDateTime": "2024-09-24T15:30:45.123456"
}
```

## 9. 테스트

프로젝트에는 Kotest를 사용한 테스트 코드가 포함되어 있습니다. 다음 명령으로 테스트를 실행할 수 있습니다:

```bash
./gradlew :light:test --tests "*McpServerTest*"
```