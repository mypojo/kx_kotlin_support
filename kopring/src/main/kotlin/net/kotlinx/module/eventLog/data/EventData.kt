package net.kotlinx.module.eventLog.data

import net.kotlinx.core.gson.GsonData

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
     * 이벤트의 소스
     * ex) jpa (JPA 리스터)
     * ex) class (카드결제 SDK 등의 프록시 객체)
     * ex) event (소스코드 삽입)
     */
    var source: String = ""

    /**
     * 이벤트의 대분류
     * ex) entityName
     * ex) className
     * ex) login
     */
    var g1: String = ""

    /**
     * 이벤트의 소분류
     * ex) I,U,D
     * ex) methodName
     * ex) 일반로그인/역할전환
     */
    var g2: String = ""

    /**
     * 이벤트 데이터의 ID
     * 검색의 주체가 되는 값
     * ex) DB의 PK (GUID)
     * ex) memberId / campId
     */
    var id: String = ""

    /**
     * 이벤트의 in
     * ex) jpa : 더티체크 정보
     * ex) method args
     */
    lateinit var x: GsonData

    /**
     * 이벤트의 out
     * ex) jpa : 변경후 엔티티
     * ex) result
     */
    lateinit var y: GsonData

}