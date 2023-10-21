package net.kotlinx.spring.tx

import com.amazonaws.services.dynamodbv2.LockItem
import net.kotlinx.aws.javaSdkv2.dynamoLock.DynamoLockModule
import net.kotlinx.aws.javaSdkv2.dynamoLock.DynamoLockReq
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


/**
 * 분산락 간단 적용 with 스프링 트랜잭션
 */
fun DynamoLockModule.withSpringTx(req: DynamoLockReq, block: (LockItem) -> Unit) {

    check(TransactionSynchronizationManager.isSynchronizationActive()) { "스프링 트랜잭션이 활성화 되어있어야합니다." }
    check(!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) { "쓰기 트랜잭션이 활성화 되어있어야합니다." }

    val lockItem = this.acquireLock(req)
    block(lockItem)
    TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
        override fun afterCompletion(status: Int) {
            //성공 여부와는 상관없이 락을 반환한다.
            lockItem.close()
        }
    })

}