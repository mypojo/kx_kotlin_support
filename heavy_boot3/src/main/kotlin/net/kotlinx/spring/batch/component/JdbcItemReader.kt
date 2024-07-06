package net.kotlinx.spring.batch.component

import net.kotlinx.spring.batch.BatchExecutor
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * 원래 있는애가 너무 구려서 비슷하게 새로 만들었다.
 * 소스보니까 afterPropertiesSet() 은 따로 안해도 된다. null체크만 하네..
 */
class JdbcItemReader<T> : JdbcCursorItemReader<T>() {

    init {
        setFetchSize(DEFAULT_FETCH_SIZE)
        setRowMapper(ANY_MAPPER as RowMapper<T>)
        setVerifyCursorPosition(false) // mysql 페치 옵션시 이거 체크해야함
    }

    /**
     * MYSQL은 스트리밍 처리 기본젹으로 안해준다.  아래 세팅을 해야 인식하며 그나마도 커서 포지션 처리가 없다.
     * https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-implementation-notes.html
     * The combination of a forward-only, read-only result set, with a fetch size of Integer.MIN_VALUE serves as a signal to the driver to stream result sets row-by-row. After this, any result sets created with the statement will be retrieved row-by-row.
     */
    fun mysql() {
        setFetchSize(Int.MIN_VALUE)
        setVerifyCursorPosition(false)
    }

    /**
     * 강제로 동기화한다.
     */
    @Synchronized
    @Throws(Exception::class)
    override fun read(): T? {
        return super.read()
    }

    /**
     * 특수 메소드. 쓸데가 많아서 일단 둔다.
     * 테스트 필요!!
     */
    fun readAll(): List<T> {
        val list = mutableListOf<T>()
        val reader = this
        BatchExecutor {
            itemReader = reader as ItemReader<out Any>
            itemWriter = ItemWriter { list.addAll(it as List<T>) }
        }
        return list
    }

    //======================== fluent 메소드 =========================
    fun opn(): JdbcItemReader<T> {
        open(ExecutionContext())
        return this
    }

    fun rowMapper(rowMapper: RowMapper<T>): JdbcItemReader<T> {
        setRowMapper(rowMapper)
        return this
    }

    /**
     * setMaxItemCount는 컨텍스트 기준인듯?
     */
    fun maxRows(maxRows: Int): JdbcItemReader<T> {
        setMaxRows(maxRows)
        return this
    }

    companion object {

        /**
         * 성능이 높아지면 올리자.
         */
        private const val DEFAULT_FETCH_SIZE = 10000

        /** 기본 매퍼 */
        val ANY_MAPPER = JdbcAnyArrayMapper()
    }

    /** 간이 매퍼 */
    class JdbcAnyArrayMapper : RowMapper<Array<Any?>> {

        override fun mapRow(rs: ResultSet, index: Int): Array<Any?> {
            val rsmd = rs.metaData
            val columnCount = rsmd.columnCount
            val array = arrayOfNulls<Any>(columnCount)
            for (i in 1..columnCount) {
                array[i - 1] = rs.getObject(i)
            }
            return array
        }
    }

}
