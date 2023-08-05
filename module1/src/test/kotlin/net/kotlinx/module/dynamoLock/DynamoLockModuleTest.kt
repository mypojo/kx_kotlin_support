package net.kotlinx.module.dynamoLock

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.javaSdkv2.AwsJavaSdkV2Client
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class DynamoLockModuleTest : TestRoot() {

    val awsConfig = AwsConfig(profileName = "sin")

    @Test
    fun test() {

        val client = AwsJavaSdkV2Client(awsConfig)

        val lockModule = DynamoLockModule(client.ddb) {
            tableName = "dist_lock-dev"
        }

        val lock = lockModule.acquireLock {
            pk = "aa"
            sk = "bb"
            div = "test"
            comment = "x pay module"
        }

        Thread.sleep(10.seconds.inWholeMilliseconds)

        lock.close()


    }

}