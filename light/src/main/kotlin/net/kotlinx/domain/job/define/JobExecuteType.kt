package net.kotlinx.domain.job.define

enum class JobExecuteType {

    /** job을 DDB에 저장하는 방식 */
    NORMAL,

    /**
     * job을 DDB에 저장하지 않음
     * ex) 람다가 이벤트 브릿지 수신시, 따로 새로운 람다를 트리거하지 않고 직접 동기화해서  실행
     * */
    NOLOG,
}