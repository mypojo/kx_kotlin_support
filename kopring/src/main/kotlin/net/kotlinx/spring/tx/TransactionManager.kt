package net.kotlinx.spring.tx

import com.google.common.base.Preconditions
import mu.KotlinLogging
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * 스프링 트랜잭션과 연동해서 DB처리가 끝난 후에 다수의 큐 입력, 이메일 전송 등의 행위를 하기 위해 만들었다.  (registerSynchronization를 여러번 해도 되지만 의도한바가 아니라고 생각함)
 *
 * 참고할 클래스.
 * AbstractPlatformTransactionManager
 *
 * triggerBeforeCommit => triggerBeforeCompletion => triggerAfterCommit => triggerAfterCompletion => clear 순으로 진행됨
 * 결과 콜백에서는 바인딩된 리소스는 다 사라진다.
 *
 * !!!! 이하 경고사항  !!!!
 * 1. 스프링 트랜잭션(스래드 로컬)이 작동하지 않으면 이것도 작동하지 않는다.
 * 2. DB트랜잭션을 기준으로 하는것임으로 이 안에서 ThreadLocal 기반의 DB리소스를 써서는 안된다. DB리소스 당겨봐야 이미 커밋된 이후이다.
 *
 *
 * TransactionSynchronizationManager.isSynchronizationActive() : initSynchronization()가 실행되었는지 여부
 * TransactionSynchronizationManager.isActualTransactionActive() : 실제 트랜잭션이 시작되었는지 여부.  시작은 트랜잭션이 doBegin() 된 이후 호출됨
 * 사실 두개 거의 같이 호출됨
 */
class TransactionManager(
    private val callback: (Boolean, List<*>) -> Unit
) {

    private val log = KotlinLogging.logger {}

    /** 스래드 로컬임으로 동기화 할 필요는 없다  */
    fun addData(data: Any) {
        Preconditions.checkState(TransactionSynchronizationManager.isActualTransactionActive(), "Transaction required")
        val datas = DATAS.get() ?: run {
            mutableListOf<Any>().apply {
                DATAS.set(this)
                log.debug("최초 데이터 입력 => registerSynchronization 시작")
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                    override fun afterCompletion(status: Int) {
                        try {
                            val success: Boolean = TransactionSynchronization.STATUS_COMMITTED == status
                            val datas: List<*> = DATAS.get() ?: emptyList<Any>()
                            callback(success, datas)
                        } finally {
                            DATAS.remove()
                            val code = if (TransactionSynchronization.STATUS_COMMITTED == status) "STATUS_COMMITTED" else "STATUS_ROLLED_BACK"
                            log.debug { "[$code] => registerSynchronization (afterCompletion) 종료 " }
                        }
                    }
                })
            }
        }
        datas.add(data)
        log.trace("현재 스래드 추가 데이터(전체 {}건) 입력", datas.size)
    }

    companion object {
        private val DATAS = ThreadLocal<MutableList<Any>>()
    }
}
