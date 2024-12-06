package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.DataSetImportMode
import aws.sdk.kotlin.services.quicksight.model.InputColumnDataType
import net.kotlinx.core.Kdsl

class QuicksightDataSetConfig {

    @Kdsl
    constructor(block: QuicksightDataSetConfig.() -> Unit = {}) {
        apply(block)
    }

    lateinit var dataSourceId: String
    lateinit var dataSetId: String
    lateinit var dataSetName: String
    var importMode: DataSetImportMode = DataSetImportMode.Spice

    lateinit var query: String

    lateinit var columns: Map<String, InputColumnDataType>

    /** 폴더 없으면 안넣어도 됨 */
    var folderIds: List<String> = emptyList()

    /** 누구한테 권한 줄지 */
    lateinit var users: List<String>

}