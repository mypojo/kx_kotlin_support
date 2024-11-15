package net.kotlinx.aws.athena

import download
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.presignGetObject
import net.kotlinx.aws.s3.s3
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import java.util.*

class AthenaMs949Test : BeSpecLight() {


    init {
        initTest(KotestUtil.FAST)

        Given("AthenaMs949Test") {
            Then("쿼리결과 다운로드 (한글적용)") {
                val download = athenaModule97.download {
                    query = """
                        SELECT nvm_camp_name "캠페인 명", nvm_camp_id "캠페인ID" 
                        FROM xx.nv_camp_data 
                        WHERE basic_date = '20240801' 
                        limit 10
                    """
                    uploadInfo = S3Data("$findProfile97-work-dev", "athena/outputLocation/${UUID.randomUUID()}/쿼리결과.csv") to null
                }
                val data = download.uploadInfo!!.first
                val presign = athenaModule97.aws.s3.presignGetObject(data.bucket, data.key)
                log.info { "링크 : $presign" }
            }
        }
    }


}
