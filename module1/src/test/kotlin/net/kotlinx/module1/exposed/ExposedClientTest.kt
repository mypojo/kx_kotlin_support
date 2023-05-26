package net.kotlinx.module1.exposed

import aws.sdk.kotlin.services.secretsmanager.getSecretValue
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.gson.toGsonData
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test


/**
 * 참고
 * https://github.com/LarryJung/study-git/tree/main/exposed/src/main/kotlin
 * */
internal class ExposedClientTest {

    @Test
    fun test() {

        val aws = AwsConfig(profileName = "sin").toAwsClient()
        val password = runBlocking {
            aws.sm.getSecretValue { this.secretId = "sin-rds_secret-dev" }.secretString!!.toGsonData()["password"].str!!
        }

//        HikariConfig().apply {
//            jdbcUrl = "jdbc:mariadb://localhost:33061/sin_dev"
//            driverClassName = "org.mariadb.jdbc.Driver"n
//            username = "admin"
//            this.password = password
//        }.also {
//            Database.connect(HikariDataSource(it))
//        }

        val db = Database.connect("jdbc:mariadb://localhost:33061/sin_dev", "org.mariadb.jdbc.Driver", "admin", password)
        transaction(db) {

            // Statements here
            addLogger(StdOutSqlLogger)

            //SchemaUtils.create (Cities)

//            val stPete = City.new {
//                name = "St. Petersburg"
//                desc = "한글 이름 abc"
//            }
//            City.new {
//                name = "St. Petersburg2"
//                desc = "한글 이름 abc 2"
//            }

            City.all().forEach {
                println(it)
                it.name = it.name + "#"
            }

            println("Cities: ${City.all()}")
        }

    }

    object Cities : IntIdTable() {
        val name = varchar("name", 50)
        val desc = varchar("desc", 50)
    }

    class City(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<City>(Cities)

        var name by Cities.name
        var desc by Cities.desc
    }

}