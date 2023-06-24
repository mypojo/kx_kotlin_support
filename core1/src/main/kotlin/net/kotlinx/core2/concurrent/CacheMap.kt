package net.kotlinx.core2.concurrent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 자주 사용되는 락 패턴을 구현한 캐시용 맵
 * map의 개별 속성 수정은 불가능함. DB 데이터 등을 메모리에 놓고 사용하는 용도
 *
 * 요구사항
 * 1. 리드/라이트락 분리
 * 2. 사용자가 원하는때 벌크 리로드가 빠르게 되어야함
 */
class CacheMap<T>(
    /** 로드는 시간이 오래걸려도 된다. 불변 객체를 리턴할것!  */
    private val loader: () -> MutableMap<String, T> = { mutableMapOf() }
) {
    //=================================================== 설정파일 ===================================================

    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()

    /** 위임 객체 */
    private lateinit var delegate: MutableMap<String, T>

    //=================================================== 통계치 ===================================================
    private val hitCount = AtomicLong()

    private val missCount = AtomicLong()

    private var loadCount: Long = 0

    private var totalLoadTime: Long = 0

    //=================================================== 메소드 ===================================================
    fun reload() {
        val start = System.currentTimeMillis()
        val newData = loader()
        val end = System.currentTimeMillis()
        writeLock.lock()
        try {
            delegate = newData
            loadCount++
            totalLoadTime += end - start
        } finally {
            writeLock.unlock()
        }
    }

    /** 직접 단건을 입력해야 할 수도 있다. ex)  IP블록  */
    fun put(key: String, value: T): T? {
        writeLock.lock()
        return try {
            delegate.put(key, value)
        } finally {
            writeLock.unlock()
        }
    }

    operator fun get(key: String): T? {
        readLock.lock()
        return try {
            val value = delegate[key]
            if (value == null) missCount.incrementAndGet() else hitCount.incrementAndGet()
            value
        } finally {
            readLock.unlock()
        }
    }
}
