package net.kotlinx.aws.athena

import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.core.Kdsl
import net.kotlinx.file.slash
import java.io.File
import java.util.*


/**
 * MS949로 다운로드 설정
 * AthenaMs949Support.kt 참고
 *  */
@Deprecated("사용안함 -> flow를 쓰세요")
class AthenaMs949 {

    @Kdsl
    constructor(block: AthenaMs949.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 요청 정보 ======================================================

    /** 아테나 모듈 */
    lateinit var athenaModule: AthenaModule

    /** 쿼리 */
    lateinit var query: String

    /** 이게 있으면 결과피일을 S3로 업로드함 */
    var uploadInfo: Pair<S3Data, Map<String, String>?>? = null

    /**
     * 압축 여부
     * 용량이 클경우 true로 설정해서 압축 해주세요. 보통 50mb 이상이면 압축 해야함!
     *  */
    var gzip: Boolean = false

    /** 라인단위 처리 */
    var processor: (List<String>) -> List<String> = { it }

    //==================================================== 내부 사용 (결과) ======================================================

    /** 쿼리 결과 주소 */
    lateinit var queryResultPath: S3Data

    /** 쿼리 결과 파일 (이름은 의미없음) */
    var queryResultFile: File = AwsInstanceTypeUtil.INSTANCE_TYPE.root.slash("AthenaMs949").slash("${UUID.randomUUID()}.csv")

}