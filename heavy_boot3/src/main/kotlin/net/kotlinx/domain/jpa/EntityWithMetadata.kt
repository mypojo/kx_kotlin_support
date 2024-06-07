package net.kotlinx.domain.jpa


import jakarta.persistence.*
import net.kotlinx.json.gson.NotExpose
import net.kotlinx.koin.Koins.koin

/**
 * 기본 메타데이터가 관리되어야 하는 엔티티
 */
@MappedSuperclass
abstract class EntityWithMetadata<T> : EntityWithId<T>() {
    //==================================================== get / set ======================================================
    /**
     * updatable = false 가 기본이다
     */
    @AttributeOverrides(
        AttributeOverride(name = "name", column = Column(name = "reg_name", length = 255, updatable = false)),
        AttributeOverride(name = "time", column = Column(name = "reg_time", updatable = false)),
        AttributeOverride(name = "id", column = Column(name = "reg_id", updatable = false)),
        AttributeOverride(name = "ip", column = Column(name = "reg_ip", length = 39, updatable = false)), // IPv4 주소: 15자리  / IPv6 주소: 39자리
    )
    @Embedded
    @NotExpose
    lateinit var reg: BasicMetadata

    @AttributeOverrides(
        AttributeOverride(name = "name", column = Column(name = "update_name", length = 255)),
        AttributeOverride(name = "time", column = Column(name = "update_time")),
        AttributeOverride(name = "id", column = Column(name = "update_id")),
        AttributeOverride(name = "ip", column = Column(name = "update_ip", length = 39)),
    )
    @Embedded
    @NotExpose
    var update: BasicMetadata? = null

    /**
     * 시간은 Holder 기준으로 입력되어야함 -> @CreatedDate / @LastModifiedDate 등의 메소드를 쓰기 힘들다
     */
    @PrePersist
    fun prePersist() {
        check(isNew)
        reg = koin<BasicMetadataFactory>().createMetadata()
        preUpdate()
    }

    @PreUpdate
    fun preUpdate() {
        update = koin<BasicMetadataFactory>().createMetadata()
    }

    @PreRemove
    fun preRemove() {

    }

}
