package net.kotlinx.module.job.define

import com.google.common.eventbus.EventBus
import net.kotlinx.aws1.MyAws1
import net.kotlinx.core.test.MyInstance
import net.kotlinx.module.job.JobFactoryDefault
import net.kotlinx.module.job.trigger.JobConfig

object MyJob {

    val JOB_CONFIG = JobConfig(
        aws = MyAws1.AWS,
        awsInfoLoader = MyAws1.AWS_INFO_LOADER,
        eventBus = EventBus(),
        jobDefinitionRepository = MyJobDef.JOBS,
        jobFactory = JobFactoryDefault(MyInstance.ID_GENERATOR, MyJobDef.JOBS)
    )


}
