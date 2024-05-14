package net.kotlinx.spring.jpa

import jakarta.persistence.Embeddable
import java.time.LocalDateTime
import java.util.*


@Embeddable
class BasicMetadata {

    /**
     * 웹 : 요청이 입력된 menuId
     * 배치 : job name
     */
    var name: String? = null

    /** 발생 시간.  ex)  입력시간, 수정시간..  */
    var time: LocalDateTime? = null

    /**
     * 웹 : 요청자의 실제 로그인 ID (대행 로그인 ID 아님)
     * 배치 : 가능하다면 실제 요청자의 ID 입력
     * ThreadLocal 에서 가져옴
     */
    var id: Long? = null

    /**
     * 웹 : 요청 사용자의 IP
     * 배치 : 하드웨어 머신 정보
     * ThreadLocal 에서 가져옴
     */
    var ip: String? = null


    //==================================================== eq (value 판정이라 모든 데이터를 비교해야함) ======================================================
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as BasicMetadata
        return id == that.id && ip == that.ip && time == that.time && name == that.name
    }

    override fun hashCode(): Int {
        return Objects.hash(id, ip, time, name)
    }

}
