package net.kotlinx.awscdk

import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps


data class CdkProject(
    /** AWS ID ex) xxxxxxxxxxx */
    val awsId: String,
    /** 이거 기반으로 네이밍 */
    val projectName: String,
    /** 리즌 */
    val region: String = REGION_KR,
) {

    /** 간단 props 리턴.  리즌 변경 버전이 필요할때 있음 주의!  */
    fun toProps(region: String = this.region): StackProps {
        val environment = Environment.builder().account(awsId).region(region).build()
        return StackProps.builder().env(environment).build()
    }

    companion object {

        /** 한국-서울 */
        const val REGION_KR: String = "ap-northeast-2"

        /** 북미서버 메인 (인증서 등록 등) */
        const val REGION_US: String = "us-east-1"

    }

}