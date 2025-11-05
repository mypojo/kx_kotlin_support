package net.kotlinx.aws.athena

/**
 * AWS Athena에서 사용 가능한 모든 데이터 타입을 문자열 상수로 정의한 객체.
 *
 * 참고:
 *  - Athena는 PrestoSQL 기반이므로, Presto에서 지원하는 데이터 타입을 대부분 동일하게 사용 가능.
 *  - 복합 타입(ARRAY, MAP, ROW)은 제네릭 형태로 정의 가능.
 *
 * 샘플 사용:
 *  val columnType = AthenaTypes.INT
 *  println("컬럼 타입: $columnType") // 출력: int
 */
object AthenaTypes {

    /**
     * 정수형 (32비트)
     * 예: 123
     */
    const val INT: String = "int"

    /**
     * 큰 정수형 (64비트)
     * 예: 9223372036854775807
     */
    const val BIGINT: String = "bigint"

    /**
     * 부호 없는 큰 정수형 (64비트)
     * 예: 18446744073709551615
     */
    const val BIGINT_UNSIGNED: String = "bigint unsigned"

    /**
     * 작은 정수형 (16비트)
     * 예: 32767
     */
    const val SMALLINT: String = "smallint"

    /**
     * 아주 작은 정수형 (8비트)
     * 예: 127
     */
    const val TINYINT: String = "tinyint"

    /**
     * 실수형 (32비트 부동소수점)
     * 예: 3.14
     */
    const val FLOAT: String = "float"

    /**
     * 배정밀도 실수형 (64비트 부동소수점)
     * 예: 3.14159265358979
     */
    const val DOUBLE: String = "double"

    /**
     * 고정소수점 (소수 자릿수 지정 가능)
     * 예: decimal(10,2) => 12345678.90
     */
    const val DECIMAL: String = "decimal"

    /**
     * 불리언 (참/거짓)
     * 예: true, false
     */
    const val BOOLEAN: String = "boolean"

    /**
     * 문자열
     * 예: "Hello World"
     */
    const val VARCHAR: String = "varchar"

    /**
     * 고정 길이 문자열
     * 예: char(5) => 'Hello'
     */
    const val CHAR: String = "char"

    /**
     * 긴 텍스트 (크기 제한 없음)
     * 예: '이것은 매우 긴 문자열입니다...'
     */
    const val STRING: String = "string"

    /**
     * 날짜
     * 예: DATE '2025-08-14'
     */
    const val DATE: String = "date"

    /**
     * 타임스탬프 (날짜 + 시간, 타임존 없음)
     * 예: TIMESTAMP '2025-08-14 12:34:56'
     */
    const val TIMESTAMP: String = "timestamp"

    /**
     * 타임스탬프 (타임존 포함)
     * 예: TIMESTAMP WITH TIME ZONE '2025-08-14 12:34:56 Asia/Seoul'
     */
    const val TIMESTAMP_WITH_TIME_ZONE: String = "timestamp with time zone"

    /**
     * 시간 (날짜 없이 시:분:초)
     * 예: TIME '12:34:56'
     */
    const val TIME: String = "time"

    /**
     * 시간 (타임존 포함)
     * 예: TIME WITH TIME ZONE '12:34:56 Asia/Seoul'
     */
    const val TIME_WITH_TIME_ZONE: String = "time with time zone"

    /**
     * 배열 타입
     * 예: ARRAY<INT> => [1, 2, 3]
     */
    const val ARRAY: String = "array"

    /**
     * 비정형 값 매핑에 주로 사용됨
     */
    const val MAP_STRING: String = "map<string,string>"

    /**
     * 비정형 값 매핑에 주로 사용됨
     * ex) 리포트 전환 값
     */
    const val MAP_BINT: String = "map<string,bigint>"

    /**
     * 구조체 타입 (컬럼 그룹)
     * 예: ROW(name VARCHAR, age INT) => {name='홍길동', age=30}
     */
    const val ROW: String = "row"

    /**
     * JSON 타입 (문자열로 저장)
     * 예: '{"name": "홍길동", "age": 30}'
     */
    const val JSON: String = "json"
}