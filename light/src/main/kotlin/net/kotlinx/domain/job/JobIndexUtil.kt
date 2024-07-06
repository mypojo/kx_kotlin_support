package net.kotlinx.domain.job

object JobIndexUtil {

    /**
     * global index pk+sk만 저장함
     * 전체 진행상태 모니터링
     * ex) 전체 진행중인 잡
     * ex) xx job 중에서 실패건 조회
     */
    const val GID_STATUS = "gidx-jobStatus-pk"

    /**
     * local index & sk만 저장함.
     * UI 페이징 처리 조회용
     * ex) user xx가 요청한 작업을 최근 작업순으로 조회
     */
    const val LID_MEMBER = "lidx-memberReqTime"

    /** 인덱스 키값 */
    val LID_MEMBER_NAME = LID_MEMBER.substringAfterLast("-")


}