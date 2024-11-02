package net.kotlinx.domain.batchTask

import net.kotlinx.domain.job.Job
import net.kotlinx.reflect.name


object BatchTaskJobUtil {

    /**
     * SFN에서 BatchTaskExecutor 를 실행하는 Job의 SFN name 접미어
     * SFN 이벤트 필터링 기준이 된다
     * */
    val PREFIX = "${BatchTaskExecutor::class.name()}-${Job::class.name()}."


}