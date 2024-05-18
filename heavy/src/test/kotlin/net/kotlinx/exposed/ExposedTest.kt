package net.kotlinx.exposed

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import org.jetbrains.exposed.sql.Table

class ExposedTest : BeSpecLog() {

    object Users : Table() {
        val id = uuid("id").autoGenerate()
        val name = varchar("name", 50)
        val hash = text("hash", "px")
        val email = varchar("email", 100).uniqueIndex()
        val passwordHash = binary("password_hash", 64)
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("Exposed") {

            Then("아직 쓸일이 없다.. 쓰고싶어.") {



            }
        }
    }

}