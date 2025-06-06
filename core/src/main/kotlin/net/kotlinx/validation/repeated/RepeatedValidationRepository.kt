package net.kotlinx.validation.repeated

import net.kotlinx.collection.addAndGet
import net.kotlinx.core.Kdsl


/**
 * Validation 저장소
 * 다수의 저장소를 등록해서 사용가능
 * ex) 람다함수용, 풀로직 체크 등등
 * */
class RepeatedValidationRepository {

    @Kdsl
    constructor(block: RepeatedValidationRepository.() -> Unit = {}) {
        apply(block)
    }

    /** 내부사용  */
    private val _allValidations: MutableList<RepeatedValidation> = mutableListOf()

    /** 전체 벨리데이션 */
    val allValidations: List<RepeatedValidation>
        get() = _allValidations

    /** 등록 */
    fun regist(block: RepeatedValidation.() -> Unit = {}): RepeatedValidation = _allValidations.addAndGet { RepeatedValidation(block) }

}