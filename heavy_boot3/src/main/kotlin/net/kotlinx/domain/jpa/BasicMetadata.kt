package net.kotlinx.domain.jpa

import jakarta.persistence.Embeddable
import java.time.LocalDateTime


@Embeddable
data class BasicMetadata(
    /**
     * 웹 : 요청이 입력된 menuId -> web Holder
     * 배치 : job name -> job Holder
     */
    val name: String,

    /**
     * 발생 시간.  ex)  입력시간, 수정시간..
     * 보통 EventDataHolder 에서 가져옴
     *  */
    val time: LocalDateTime,

    /**
     * 웹 : 요청자의 실제 로그인 ID (대행 로그인 ID 아님) ->  spring security
     * 배치 : 가능하다면 실제 요청자의 ID 입력 -> job Holder
     */
    val id: Long,

    /**
     * 웹 : 요청 사용자의 IP ->  spring mvc
     * 배치 : 하드웨어 머신 정보 ->  AWS Instance
     */
    val ip: String,
)
