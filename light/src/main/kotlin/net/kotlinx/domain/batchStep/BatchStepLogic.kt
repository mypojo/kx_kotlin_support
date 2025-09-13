package net.kotlinx.domain.batchStep

import net.kotlinx.json.gson.GsonData

/**
 * 배치스탭 동작들
 * SFN 으로 작동하는거 그냥 퉁쳐서 이걸로 정의한다
 *  */
interface BatchStepLogic {

    suspend fun execute(input: GsonData): Any

}