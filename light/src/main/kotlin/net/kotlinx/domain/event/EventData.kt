package net.kotlinx.domain.event

import net.kotlinx.json.gson.GsonData

/**
 * 상세는 표 참고
 * flat을 쉽게하기 위해서 모든 데이터를 동일 네이밍으로 통일함
 *
 * http 호출의 경우 SDK나 http 클라이언트 등에  자체 리트라이가 있는지 확인해야함
 *
 * div,group,in 등은 예약어라서 사용 못함
 */
class EventData {

    /**
     * 이벤트를 발생시키는 기술적인 출처
     * ex) jpa (JPA 리스너)
     * ex) eventbus (구아바 이벤트버스)
     * ex) http_proxy (카드결제 SDK 등의 프록시 객체)
     * ex) code (소스코드로 직접 삽입)
     */
    var from: String = ""

    /**
     * group01 의 줄임말 (json 용량때문)
     * 이벤트의 대분류 -> 그룹바이 해서 예쁘게 나올정도
     * ex) jpa : ${DB테이블명},
     * ex) ${서비스명}
     * ex) login, audit, rolwSwitch ..
     * ex) api_log, api_pg_toss
     */
    var g1: String = ""

    /**
     * 이벤트의 소분류 -> 그룹바이 해서 예쁘게 나올정도
     * jpa : ${I,U,D}  ,
     * ${methodName} , ${개인정보_식별번호}
     * ex) /aa/bb/cc
     * ex) 일반로그인/역할전환
     */
    var g2: String = ""

    /**
     * 이벤트 데이터의 ID -> 검색의 주체가 되는 값
     * jpa : DB의 PK (GUID)
     * ex) memberId / campId
     * level2 테이블은 여기로 인덱스가 걸릴 수 있음
     */
    var id: String = ""

    /**
     * 이벤트의 in
     * jpa : 더티체크 정보
     * ex) method args
     * ex) http req body / query
     */
    lateinit var x: GsonData

    /**
     * 이벤트의 out
     * jpa : 변경후 엔티티
     * ex) result
     */
    lateinit var y: GsonData

}