package net.kotlinx.aws.athena.table


/**
 * 파티션 많이쓰면 느려짐
 *
 * ex) 시계열의 경우 “날짜” 정도만 사용.
 * ex) 계정 & “월” 정도만 사용
 *
 * 주의!!! 인덱스끼리 상호 호환 안됨!
 * -> INDEX 로 저장시 PROJECTION 으로 읽을 수 없음 (PROJECTION은 내부적으로 파라메터에 = 조회를 하기때문)
 *
 *
 * INDEX & PROJECTION 둘다, 파티션이 뒤에 붙어서 나오게됨
 * */
enum class AthenaTablePartitionType {

    /** 파티션 없음 */
    NONE,

    /**
     * 정규 파티션 사용. 예측 불가능한 값으로 파티셔닝 되고, 범위 조회를 해야하는 경우
     *     ex) “날짜” / “사용자ID” 로 파티셔닝 → “날짜” 별 전체 사용자 ID를 조회 해야함 -> 이경우 파티션정보 선조회 후 실제 S3조회해서 쿼리 리턴
     * 인덱스의 인덱스를 안달면 파티션 조회에만 10초 이상 걸림 → 인덱스 걸면 2.2초
     * 아이스버그가 나오면서 이거로 하는 경우는 이제 거의 없음
     * */
    INDEX,

    /**
     * 주의!! INDEX 로 만들어진 external 테이블을 PROJECTION 로 읽을 수 없음 (파티션 정보 누락)
     * 굉장히 파티션이 많을경우(날짜/회원ID 로 분류) ->  PROJECTION 으로 해야  빠름
     * 지원되는 형식 : https://docs.aws.amazon.com/ko_kr/athena/latest/ug/partition-projection-supported-types.html
     *
     * 주의!!
     * between 같은거는 date 나 range 로 넣어야 작동함 -> AWS가 모든 사이값들을 대입해서 쿼리에 추가하는 방식임 -> 절반이하로 비어있으면 인덱스 방식을 추천하지만 안쓸래..
     * */
    PROJECTION,
    ;


}
