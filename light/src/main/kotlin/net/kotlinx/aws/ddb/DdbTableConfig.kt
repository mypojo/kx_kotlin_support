package net.kotlinx.aws.ddb

import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl


/**
 * DDB 객체를 입력 / 조회 하는데 필요한 각종 정보 모음
 * */
class DdbTableConfig<T> {

    @Kdsl
    constructor(block: DdbTableConfig<T>.() -> Unit = {}) {
        apply(block)
    }

    /** 테이블명  */
    lateinit var tableName: String

    /** 객체 변환기 */
    lateinit var converter: DdbConverter<T>

    //==================================================== 거의 고정 ======================================================

    /** PK 컬럼 이름.  */
    var pk: String = PK

    /** SK 컬럼 이름.  */
    var sk: String = SK

    /** 리전  */
    var region: String = AwsConfig.REGION_KR

    //====================================================  ======================================================

    /** 콘솔링크 */
    fun toConsoleLink(data: DdbData): String = DdbUtil.toConsoleLink(tableName, data, region)

    companion object {
        const val PK = "pk"
        const val SK = "sk"
    }


}