package net.kotlinx.kopring.spring.session

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.deleteItem
import net.kotlinx.aws.dynamo.getItem
import net.kotlinx.aws.dynamo.putItem
import org.apache.commons.lang3.tuple.Pair
import org.springframework.session.SessionRepository
import java.io.*
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 캐싱되는 DDB 세션 저장소.
 * 실시간 변경 감지용은 아니니, 스티키 세션을 꼭 켜도록 하자
 */
class DynamoDBSessionRepository(
    private val dynamo: DynamoDbClient,
    /** expire 시간 */
    val maxInactiveIntervalInSeconds: Int,
    /**  이 시간동안 서버간의 동기화가 깨짐. 클수록 DDB 사용 리소스가 적어짐 */
    inMemoryCacheExpireSec: Int
) : SessionRepository<DynamoDbSession> {

    private val log = KotlinLogging.logger {}

    /**
     * 너무 잦은 호출을 방지하기위한 캐시. 일단 이렇게 넘어간다.
     * 세션 정보 업데이트시 객체 내용이 같이 변하기때문에 더티체크를 위해서 래퍼런스 외에 hash값도 같이 저장한다
     */
    private val sessionCache: Cache<String, Pair<Int, DynamoDbSession>> = CacheBuilder.newBuilder().expireAfterWrite(inMemoryCacheExpireSec.toLong(), TimeUnit.SECONDS).build()
    private val hitCounter = AtomicLong()

    override fun createSession(): DynamoDbSession {
        val dynamoDBSession = DynamoDbSession(UUID.randomUUID().toString(), maxInactiveIntervalInSeconds.toLong())
        log.debug { "createSession ${dynamoDBSession.id}" }
        return dynamoDBSession
    }

    @Synchronized
    override fun save(session: DynamoDbSession) {
        val cached = sessionCache.getIfPresent(session.id)
        val currentHashCode = session.hashCode()
        val existHashCode = if (cached == null) 0 else cached.key
        val equals = currentHashCode == existHashCode
        if (equals) {
            log.trace { "[${existHashCode}/${currentHashCode}] sessionCache EQ" }
            hitCounter.incrementAndGet()
            return
        }
        val item = toDynamoDBItem(session)
        runBlocking { dynamo.putItem(item) }

        sessionCache.put(session.id, Pair.of(currentHashCode, session))
        log.debug { "[${existHashCode}/${currentHashCode}] DDB session 저장 : [hitCounter] ${hitCounter.get()} : ${session.id}" }
    }

    @Synchronized
    override fun findById(id: String): DynamoDbSession? {
        val cached = sessionCache.getIfPresent(id)
        if (cached != null) {
            log.trace { "sessionCache 히트 $id" }
            hitCounter.incrementAndGet()
            return cached.value
        }
        return try {
            val sessionItem: DynamoDbSessionItem? = runBlocking { dynamo.getItem(DynamoDbSessionItem(id)) }
            if (sessionItem == null) {
                log.trace { "findById $id -> null" }
                return null
            }
            val session = toSession(sessionItem)
            if (session.isExpired) {
                log.info { "Session: '${id}' has expired. It will be deleted." }
                deleteById(id)
                log.trace { "findById $id -> null" }
                return null
            }
            session.lastAccessedTime = Instant.now()
            log.trace("findById {} -> {}", id, session.attributeNames)
            session
        } catch (e: InvalidClassException) {
            log.warn { "DDB와 소스코드의 시리얼 불일치 -> 해당 데이터를 무시합니다." }
            deleteById(id)
            null
        } catch (e: ClassNotFoundException) {
            log.warn { "DDB와 소스코드의 시리얼 불일치 -> 해당 데이터를 무시합니다." }
            deleteById(id)
            null
        }
    }

    override fun deleteById(id: String) {
        log.warn { "[로그아웃!] DDB 삭제됩니다." }
        runBlocking { dynamo.deleteItem(DynamoDbSessionItem(id)) }
        sessionCache.invalidate(id)
    }

    private fun toDynamoDBItem(session: DynamoDbSession): DynamoDbSessionItem {
        return DynamoDbSessionItem(session.id).apply {

            if (session.maxInactiveInterval.seconds >= 0) {
                val lastAccessTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(session.lastAccessedTime.toEpochMilli())
                this.ttl = lastAccessTimeSeconds + session.maxInactiveInterval.seconds
            }

            val out = ByteArrayOutputStream() //안닫아도 되나?
            ObjectOutputStream(out).use {
                it.writeObject(session)
            }
            this.data = out.toByteArray()
        }
    }

    private fun toSession(item: DynamoDbSessionItem): DynamoDbSession {
        return ObjectInputStream(ByteArrayInputStream(item.data)).use {
            it.readObject()
        } as DynamoDbSession
    }

}
