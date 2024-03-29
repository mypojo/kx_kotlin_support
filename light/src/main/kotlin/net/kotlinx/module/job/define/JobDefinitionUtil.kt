package net.kotlinx.module.job.define

import net.kotlinx.module.job.JobTasklet
import net.kotlinx.reflect.name
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatformTools
import kotlin.reflect.KClass

object JobDefinitionUtil : KoinComponent {

    /**
     * 잡 정의 가져오기
     * ex) 단위테스트 실행
     *  */
    fun findById(kclass: KClass<out JobTasklet>): JobDefinition = findById(kclass.name())

    /**
     * 잡 정의 가져오기
     * ex) 스케줄링 실행
     *  */
    fun findById(jobPk: String): JobDefinition = KoinPlatformTools.defaultContext().get().get<JobDefinition>(named(jobPk))

    /** 전체 리스팅 */
    fun list(): List<JobDefinition> = getKoin().getAll<JobDefinition>()

}

