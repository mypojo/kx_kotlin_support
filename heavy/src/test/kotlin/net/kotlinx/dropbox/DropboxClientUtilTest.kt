package net.kotlinx.dropbox

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm


class DropboxClientUtilTest : BeSpecHeavy() {


    init {
        initTest(KotestUtil.FAST)

        Given("DropboxClientUtil") {
            Then("xx") {

                val secret by lazyLoadStringSsm("/api/dropbox/key")
                val client = DropboxClientUtil.create("kotlinx", secret)

                client.listAll("/work").forEach {
                    println(it)
                }


            }
        }
    }


}
