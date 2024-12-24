package net.kotlinx.aws.quicksight

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.DeploymentType

class QuicksightSupportKtTest : BeSpecHeavy() {

    private val client by lazy { koin<AwsClient>(findProfile97) }

    val suff = DeploymentType.DEV.suff
    //val suff = DeploymentType.PROD.suff
    val dbName = suff.substring(0, 1)


    init {
        initTest(KotestUtil.IGNORE)

        Given("퀵사이트 분석") {




        }
    }

}
