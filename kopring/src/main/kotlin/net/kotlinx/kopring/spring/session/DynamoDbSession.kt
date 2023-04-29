package net.kotlinx.kopring.spring.session

import com.google.common.base.Objects
import org.springframework.session.Session
import java.io.Serializable
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

/**
 * 세션 정보
 * attributes 에 시큐리티의 context가 입력된다
 * 어디서 주워온거 같음..
 */
class DynamoDbSession(
    private var id: String,
    private var maxInactiveIntervalSeconds: Long,
) : Session, Serializable {

    private val creationTime: Long = System.currentTimeMillis()
    private var lastAccessedTime: Long = creationTime
    private val attributes: MutableMap<String, Any> = HashMap()
    private var expireAt: Date? = null

    override fun changeSessionId(): String {
        val changedId = UUID.randomUUID().toString()
        id = changedId
        return changedId
    }

    override fun getCreationTime(): Instant {
        return Instant.ofEpochMilli(creationTime)
    }

    override fun setLastAccessedTime(lastAccessedTime: Instant) {
        this.lastAccessedTime = lastAccessedTime.toEpochMilli()
        expireAt = Date.from(lastAccessedTime.plus(Duration.ofSeconds(maxInactiveIntervalSeconds)))
    }

    override fun getLastAccessedTime(): Instant {
        return Instant.ofEpochMilli(lastAccessedTime)
    }

    override fun setMaxInactiveInterval(interval: Duration) {
        maxInactiveIntervalSeconds = interval.seconds
    }

    override fun getMaxInactiveInterval(): Duration {
        return Duration.ofSeconds(maxInactiveIntervalSeconds)
    }

    override fun isExpired(): Boolean {
        return maxInactiveIntervalSeconds >= 0 && Date().after(expireAt)
    }

    override fun getId(): String {
        return id
    }

    override fun <T> getAttribute(attributeName: String): T {
        return attributes[attributeName] as T
    }

    override fun getAttributeNames(): Set<String> {
        return attributes.keys.stream()
            .collect(Collectors.toSet())
    }

    override fun setAttribute(attributeName: String, attributeValue: Any) {
        if (attributeValue == null) {
            removeAttribute(attributeName)
        } else {
            attributes[attributeName] = attributeValue
        }
    }

    override fun removeAttribute(attributeName: String) {
        attributes.remove(attributeName)
    }

    //==================================================== EQ 재정의 ======================================================
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DynamoDbSession
        return Objects.equal(id, that.id) && Objects.equal(attributes, that.attributes)
    }

    /** context 내용 비교용  */
    override fun hashCode(): Int {
        return Objects.hashCode(id, attributes)
    }
}