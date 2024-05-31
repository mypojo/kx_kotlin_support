package net.kotlinx.validation.bg

import net.kotlinx.collection.addAndGet
import net.kotlinx.core.Kdsl


/**
 * BgValidation 저장소
 * */
class BgValidationList {

    @Kdsl
    constructor(block: BgValidationList.() -> Unit = {}) {
        apply(block)
    }

    /** 내부사용  */
    private val _allValidations: MutableList<BgValidation> = mutableListOf()

    /** 전체 벨리데이션 */
    val allValidations: List<BgValidation>
        get() = _allValidations

    /** 등록 */
    fun regist(block: BgValidation.() -> Unit = {}): BgValidation = _allValidations.addAndGet { BgValidation(block) }

}