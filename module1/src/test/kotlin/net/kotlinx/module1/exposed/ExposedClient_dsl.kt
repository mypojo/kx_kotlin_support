package net.kotlinx.module1.exposed

import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.gson.toGsonData
import net.kotlinx.module1.exposed.Payments.amount
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal


/** dsl */
object Payments : LongIdTable(name = "payment") {
    val orderId = long("order_id")
    val amount = decimal("amount", 19, 4)
}

/**
 * 참고
 * https://github.com/LarryJung/study-git/tree/main/exposed/src/main/kotlin
 * */
internal class ExposedClient_dsl {

    @Test
    fun `exposed DSL`() {

        val aws = AwsConfig(profileName = "sin").toAwsClient()
        val password = runBlocking {
            aws.sm.getSecretValue { this.secretId = "sin-rds_secret-dev" }.secretString!!.toGsonData()["password"].str!!
        }

        val db = Database.connect("jdbc:mariadb://localhost:33061/sin_dev", "org.mariadb.jdbc.Driver", "admin", password)

        transaction(db) {

            // Show SQL logging
            addLogger(StdOutSqlLogger)

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            SchemaUtils.create(Payments)

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..5).map {
                Payments.insert { payments ->
                    payments[amount] = it.toBigDecimal()
                    payments[orderId] = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE payment.amount >= 0
            Payments.update({ amount greaterEq BigDecimal.ZERO })
            {
                it[amount] = BigDecimal.ZERO
            }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount = 0
            // Payment(amount=1.0000, orderId=1)
            Payments.select { amount eq BigDecimal.ZERO }
                .forEach { println(it) }

            // DELETE FROM payment WHERE payment.amount >= 1
            Payments.deleteWhere { amount greaterEq BigDecimal.ONE }

            // DROP TABLE IF EXISTS payment
            SchemaUtils.drop(Payments)


        }


    }

}