package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.GroupDefinitionType

/**
 * 통계치 데이터
 * json 변환 가능하도록 기본 값만 사용
 *  */
class CostExplorerLine(block: CostExplorerLine.() -> Unit = {}) {

    /** 프로젝트명 */
    var projectName: String? = null

    /** 시계열 */
    var timeSeries: String? = null

    /** 달러화임으로 적당히 라운드 처리 */
    var costValue: Double? = null

    /**
     * 그룹바이 형태
     * @see GroupDefinitionType
     *  */
    lateinit var groupDefinitionType: String

    /**
     * group 디멘션 키값
     * ex) 서비스명 or cost tag명
     * */
    var key: String? = null

    init {
        block(this)
    }
}