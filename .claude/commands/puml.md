PlantUML 다이어그램을 생성합니다.
사용자의 요청에 맞는 PlantUML 다이어그램을 .md 파일에 작성합니다.

## PlantUML 1.2026.2beta3 호환 규칙 (필수 준수)

### 절대 사용 금지 (Syntax Error 발생)
- `skinparam` 모든 형태 금지 (한 줄, 블록 모두)
- `package` / `rectangle` / `cloud` / `node` 안에 `component` 중첩 금지
- `actor` 와 `component` 혼합 금지
- `database` 와 `component` 혼합 금지
- `legend right ... endlegend` 금지
- `footer` 금지
- `!theme` 금지
- `<<stereotype>>` 문법 금지
- `[**bold**\ntext]` 대괄호 안 bold 금지
- `component "text" as name` 따옴표 형태 금지 (대괄호 `[...]`만 사용)

### 안전한 문법 (이것만 사용)
- `@startuml` / `@enduml`
- `title 제목`
- `component [텍스트\n줄바꿈] as alias #색상코드`
- `alias --> alias` (실선 화살표)
- `alias ..> alias` (점선 화살표)
- `alias -[#색상코드]-> alias` (색상 화살표)
- `alias -[#색상코드,bold]-> alias` (굵은 색상 화살표)
- `alias -[#색상코드,dashed]-> alias` (점선 색상 화살표)

### Activity Diagram 안전한 문법
- `|#색상| 레인이름 |` (수영장 레인)
- `start` / `stop`
- `:액션;`
- `if (조건?) then (Yes) ... else (No) ... endif`
- `fork ... fork again ... end fork`

### 색상 팔레트 (Material Design)
- Network: `#CFD8DC` / Frontend: `#A5D6A7` / Auth: `#80CBC4`
- ECS: `#90CAF9` `#64B5F6` `#BBDEFB` / Lambda: `#CE93D8` `#E1BEE7`
- Batch: `#FFCC80` `#FFE0B2` / AI: `#EF9A9A` / Messaging: `#FFAB91`
- Storage: `#FFE082` / CI/CD: `#9FA8DA` `#C5CAE9`
- External: `#FFE0B2` / Library: `#E0E0E0` / Test: `#C8E6C9`

### 범례/환경설정 처리
- PlantUML 내부에 legend/footer 넣지 않음
- 마크다운 테이블로 다이어그램 아래 별도 작성

$ARGUMENTS
