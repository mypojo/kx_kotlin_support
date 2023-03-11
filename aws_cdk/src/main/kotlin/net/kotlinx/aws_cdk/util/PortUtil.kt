package net.kotlinx.aws_cdk.util


/** 포트 하드코딩 방지용 */
object PortUtil {

    //==================================================== 웹포트 ======================================================
    /** 각 컨테이너에 오픈된 유일한 서비스 포트 & public IP 로 직접 연결되는 포트 (개별 인스턴스 테스트용)  */
    const val WEB_8080: Int = 8080

    /** 실제 서비스 웹 포트 & AWS 내부 통신 포트  */
    const val WEB_443: Int = 443

    /** 일반 웹 포트. 서비스용으로는 사용안함(https로 리다이렉트)  */
    const val WEB_80: Int = 80

    //==================================================== 기타 service 포트 ======================================================
    const val MYSQL: Int = 3306

}