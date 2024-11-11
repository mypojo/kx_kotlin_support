package net.kotlinx.kotest.modules.job

import mu.KotlinLogging
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobTableUtil
import net.kotlinx.domain.job.define.JobExecuteType
import net.kotlinx.domain.job.define.JobScheduleType
import net.kotlinx.domain.job.define.registJob
import net.kotlinx.domain.job.trigger.JobLocalExecutor
import net.kotlinx.domain.job.trigger.JobSerializer
import net.kotlinx.koin.KoinModule
import net.kotlinx.kotest.MyEnv
import net.kotlinx.kotest.modules.AwsModule.IAM_PROFILES
import net.kotlinx.reflect.name
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


object JobModule : KoinModule {

    private val log = KotlinLogging.logger {}

    override fun moduleConfig(): Module = module {

        single(named(Job::class.name())) {
            log.debug { "JobTable 생성!!" }
            JobTableUtil.createDefault {
                tableName = "job-${MyEnv.SUFFIX}"
            }
        }

        single { JobRepository() }
        single { JobSerializer() }
        single { JobLocalExecutor() }

        IAM_PROFILES.profiles.forEach { pair ->
            val profileName = pair.first
            single(named(profileName)) { JobRepository(profileName) }
            single(named(profileName)) { JobSerializer(profileName) }
            single(named(profileName)) { JobLocalExecutor(profileName) }
        }

        registJob {
            jobClass = DemoJob::class
            name = "데모 작업 실행"
            descs = listOf(
                "x분 주기로 동기화",
                "월비용 =  80원",
            )
            jobExecuteType = JobExecuteType.NOLOG
            jobScheduleType = JobScheduleType.MINUTES
        }


    }

}
