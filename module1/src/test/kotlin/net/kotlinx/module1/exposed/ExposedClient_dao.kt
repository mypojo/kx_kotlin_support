package net.kotlinx.module1.exposed

import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.gson.toGsonData
import net.kotlinx.module1.exposed.Payments.amount
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal


/** dao */
class Payment(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Payment>(Payments)

    var amount by Payments.amount
    var orderId by Payments.orderId
}

internal class ExposedClient_dao {

    @Test
    fun `exposed DAO`() {

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
            (1..20).map {
                Payment.new {
                    amount = it.toBigDecimal()
                    orderId = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE id = 1
            // ...
            Payment.all()
                .forEach { it.amount = BigDecimal.ZERO }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount >= 1
            // Payment(amount=1.0000, orderId=1)
            Payment.find { amount eq BigDecimal.ONE }
                .forEach { println(it) }

            // DELETE FROM payment WHERE payment.id = 1
            // ...
            Payment.all()
                .forEach { it.delete() }

            // DROP TABLE IF EXISTS payment
            SchemaUtils.drop(Payments)
        }

    }

}