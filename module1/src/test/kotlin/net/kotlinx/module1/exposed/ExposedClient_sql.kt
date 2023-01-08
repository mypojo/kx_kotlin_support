package net.kotlinx.module1.exposed

import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.gson.toGsonData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test


/**
 * 참고
 * https://github.com/LarryJung/study-git/tree/main/exposed/src/main/kotlin
 * */
internal class ExposedClient_sql {

    @Test
    fun test() {

        val aws = AwsConfig(profileName = "sin").toAwsClient()
        val password = runBlocking {
            aws.sm.getSecretValue { this.secretId = "sin-rds_secret-dev" }.secretString!!.toGsonData()["password"].str!!
        }

        val db = Database.connect("jdbc:mariadb://localhost:33061/sin_dev", "org.mariadb.jdbc.Driver", "admin", password)
        transaction(db) {
            addLogger(StdOutSqlLogger)



        }

    }


}