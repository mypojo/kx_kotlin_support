package net.kotlinx.domain.job

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import net.kotlinx.aws.AwsNaming
import net.kotlinx.reflect.name


/**
 * job 업무로직이 담긴 작업
 * 만약 spring-batch 가 필요한 경우라면 SFN으로 분산처리하게 할것
 */
interface JobTasklet {
    fun doRun(job: Job)

    /**
     * Task를 실행할 Job을 json 으로 구성해준다.
     * ex) 스탭스타트 or 디스패쳐 테스트용
     * */
    fun createEmptyJob(msg: String = "test"): ObjectType {
        val me = this
        return obj {
            AwsNaming.JOB_PK to me::class.name()
            //텍스트로 입력해야한다!!
            AwsNaming.JOB_OPTION to obj {
                "msg" to msg
            }.toString()
        }
    }
}