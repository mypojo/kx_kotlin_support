package net.kotlinx.domain.batchTask

import net.kotlinx.json.gson.GsonData


/** 배치 작업 */
interface BatchTaskRuntime {

    /**
     * @return 한번의 실행에 다수의 로직이 리턴될 수 있음
     * */
    suspend fun executeLogic(input: List<String>, option: GsonData): Map<String, List<GsonData>>


}