package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.DataSetImportMode
import aws.sdk.kotlin.services.quicksight.model.InputColumnDataType
import net.kotlinx.core.Kdsl


class QuicksightDataSetCreation {

    @Kdsl
    constructor(block: QuicksightDataSetCreation.() -> Unit = {}) {
        apply(block)
    }

    /** 출처 */
    lateinit var dataSourceId: String

    /** 데이터셋 ID */
    lateinit var dataSetId: String

    /** 데이터셋 NAME */
    lateinit var dataSetName: String

    /** 기본으로 Spice */
    var importMode: DataSetImportMode = DataSetImportMode.Spice

    /** 컬럼 필수임.. 자동으로 안해줌  */
    lateinit var columns: Map<String, InputColumnDataType>

    /** 폴더 없으면 안넣어도 됨 */
    var folderIds: List<String> = emptyList()

    /** 누구한테 권한 줄지 */
    lateinit var users: List<String>

    /** 기본으로 테이블 */
    var type: QuicksightDataSetConfigType = QuicksightDataSetConfigType.TABLE

    /** 로우 기반 퍼시면에 사용할 데이터세트 */
    var rowLevelPermissionDataSet: String? = null


    //==================================================== 옵션 - 테이블 매핑 ======================================================

    lateinit var schema: String
    lateinit var tableName: String


    //==================================================== 옵션 - 쿼리 매핑 ======================================================

    lateinit var query: String

    enum class QuicksightDataSetConfigType {

        /** 테이블 */
        TABLE,

        /** 쿼리로 하면 증분이 안되는듯 */
        QUERY
    }


}