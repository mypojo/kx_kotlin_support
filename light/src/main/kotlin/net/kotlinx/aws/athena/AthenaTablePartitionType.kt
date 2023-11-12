package net.kotlinx.aws.athena


/**
 * 파티션 많이쓰면 느려짐
 *
 * ex) 시계열의 경우 “날짜” 정도만 사용.
 * ex) 계정 & “월” 정도만 사용
 *
 * 주의!!! 인덱스끼리 상호 호환 안됨!
 * -> INDEX 로 저장시 PROJECTION 으로 읽을 수 없음 (PROJECTION은 내부적으로 파라메터에 = 조회를 하기때문)
 * */
enum class AthenaTablePartitionType {

    /** 파티션 없음 */
    NONE,

    /**
     * 1. 정규 파티션 사용. 예측 불가능한 값으로 파티셔닝 되고, 범위 조회를 해야하는 경우
     *     1. ex) “날짜” / “사용자ID” 로 파티셔닝 → “날짜” 별 전체 사용자 ID를 조회 해야함
     * 2. 느림 → 이틀치 데이터 조회시 5.5초
     * 3. 인덱스 안달면 파티션 조회에만 10초 이상 걸림 → 인덱스 걸면 2.2초
     * */
    INDEX,

    /**
     * 1. where 조건으로, 파티션 추측해서 작동
     * 2. 빠름 → 이틀치 데이터 조회시 3초 걸림
     * 3. storage.location.template 은 접미어 지정임
     *     1. “인덱스 스킵 가능” → 이경우 파티션으로 컬럼확인이 되지 않기 때문에 본 테이블에 데이터가 다 있어야함
     *     2. ex) basicDate, userId   파티션에서 basicDate 만으로 조회 가능
     * 4. “날짜” 베이스의 level1, level2 테이블의 경우 그냥 이걸로 할것
     *     1. 날짜별 수백 mb 넘어가지 않는 한 이게 효율적.
     *
     *  "날짜" 를 string 으로 설정할 경우 between 등의 쿼리 문제없이 잘 작동함
     * */
    PROJECTION,
    ;


}
