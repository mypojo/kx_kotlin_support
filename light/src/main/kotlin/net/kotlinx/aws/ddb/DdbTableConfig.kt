package net.kotlinx.aws.ddb

import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl
import net.kotlinx.string.encodeUrl


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
    var pkName: String = PK_NAME

    /** SK 컬럼 이름.  */
    var skName: String = SK_NAME

    /** DDB가 있는 리전  */
    var region: String = AwsConfig.REGION_KR

    //====================================================  ======================================================

    /** DDB 콘솔 링크  */
    fun toConsoleLink(ddbData: DdbData): String {
        return "https://$region.console.aws.amazon.com/dynamodbv2/home?region=$region#edit-item?table=$tableName&itemMode=2&pk=${ddbData.pk.encodeUrl()}&sk=${ddbData.sk.encodeUrl()}&route=ROUTE_ITEM_EXPLORER"
    }

    companion object {
        const val PK_NAME = "pk"
        const val SK_NAME = "sk"
    }


}