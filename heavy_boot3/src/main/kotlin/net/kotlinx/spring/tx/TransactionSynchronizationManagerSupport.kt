package net.kotlinx.spring.tx

import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


/**
 * 트랜잭션 동기화 간단버전
 * @see TransactionManager 동기화시 데이터 처리가 필요한 경우
 */
fun TransactionSynchronizationManager.synch(callback: (Boolean) -> Unit) {
    TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
        override fun afterCompletion(status: Int) {
            val success = TransactionSynchronization.STATUS_COMMITTED == status
            callback(success)
        }
    })
}