package net.kotlinx.aws.ddb

import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl


/**
 * DDB 객체를 입력 / 조회 하는데 필요한 각종 정보 모음
 * */
class DbTable {

    @Kdsl
    constructor(block: DbTable.() -> Unit = {}) {
        apply(block)
    }

    /** 테이블명  */
    lateinit var tableName: String

    /** 객체 변환기 */
    lateinit var converter: DbConverter

    //==================================================== 후크 ======================================================

    /** 입력전 전처리 ex) TTL */
    var beforePut: (DbItem) -> Unit = {}

    /** 영속화 할지 여부.  */
    var persist: (DbItem) -> Boolean = { true }

    //==================================================== 거의 고정 ======================================================

    /** PK 컬럼 이름.  */
    var pkName: String = PK_NAME

    /** SK 컬럼 이름.  */
    var skName: String = SK_NAME

    /** DDB가 있는 리전  */
    var region: String = AwsConfig.REGION_KR


    companion object {
        const val PK_NAME = "pk"
        const val SK_NAME = "sk"
    }


}