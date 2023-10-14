package net.kotlinx.module.job

object JobIndexUtil {

    /**
     * global index pk+sk만 저장함
     * 전체 진행상태 모니터링
     */
    const val GID_STATUS = "gidx-jobStatus-pk"

    /**
     * local index & sk만 저장함.
     * UI 페이징 처리 조회용
     */
    const val LID_MEMBER = "lidx-memberReqTime"


}