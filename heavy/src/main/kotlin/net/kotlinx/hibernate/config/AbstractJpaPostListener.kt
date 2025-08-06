package net.kotlinx.hibernate.config

import org.hibernate.event.spi.*
import org.hibernate.persister.entity.EntityPersister

/**
 * JPA 리스터 간단 정리용. 아무것도 안하는것들 정리
 * 리스너 정보를 이벤트브릿지로 넘길때 사용됨
 */
abstract class AbstractJpaPostListener : PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

    /** 필요하면 이거 오버라이드  */
    override fun requiresPostCommitHandling(persister: EntityPersister): Boolean {
        return true
    }

    override fun onPostInsertCommitFailed(event: PostInsertEvent) {
        //아무것도 하지 않음
    }

    override fun onPostDeleteCommitFailed(event: PostDeleteEvent) {
        //아무것도 하지 않음
    }

    override fun onPostUpdateCommitFailed(event: PostUpdateEvent) {
        //아무것도 하지 않음
    }
}