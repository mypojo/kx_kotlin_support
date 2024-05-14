package net.kotlinx.spring.jpa


import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Transient
import net.kotlinx.json.gson.NotExpose
import org.hibernate.Hibernate
import org.springframework.data.domain.Persistable
import java.util.*

/**
 * 일반적인 엔티티
 * 기본 Persistable 가 자바 버전이라서 각 JvmName를 교체해줘야 한다.
 */
@MappedSuperclass
abstract class EntityWithId<T> : Persistable<T> {

    /** ID */
    @get:JvmName("getEntityId")
    var id: T? = null

    /**
     * Persistable 인터페이스 요구사항.
     * ID는 채번해서 입력하기때문에 반드시 필요함.
     */
    @Transient
    @NotExpose
    @get:JvmName("isEntityNew")
    private var isNew = false

    //==================================================== 기본 오버라이드 ======================================================

    /** 기본 오버라이드 */
    override fun getId(): T? = id

    /** 기본 오버라이드 */
    override fun isNew(): Boolean = isNew


    /** ID만 비교 */
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false

        val persistable = o as Persistable<*>
        return id == persistable.id
    }

    /** ID만 비교 */
    override fun hashCode(): Int = Objects.hash(id)
}
