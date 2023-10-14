package net.kotlinx.module1.exposed

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.batchInsert
import org.junit.jupiter.api.Test

object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Writers : LongIdTable("writer") {
    val name = varchar("name", 150)
    val email = varchar("email", 150)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

internal class ExposedClient_batch {

    /**
     * JPA 없이 배치 입력
     * rewriteBatchedInserts 같은 옵션 필수
     *  */
    @Test
    fun `batch insert`() {
        val data = (1..10).map { it }
        Books.batchInsert(
            data,
            ignore = false,
            //shouldReturnGeneratedValues = false
        ) {
            //this[Books.writer] = 23
            this[Books.title] = "$it-title"
            this[Books.price] = it.toBigDecimal()
//            this[Books.createdAt] = LocalDateTime.now()
//            this[Books.updatedAt] = LocalDateTime.now()
        }
    }

}