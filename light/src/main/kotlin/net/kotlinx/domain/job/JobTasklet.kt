package net.kotlinx.domain.job

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import net.kotlinx.aws.AwsNaming
import net.kotlinx.exception.KnownException
import net.kotlinx.reflect.name
import kotlin.reflect.full.declaredMemberFunctions


/**
 * job 업무로직이 담긴 작업
 * ex) class DemoJob : JobTasklet
 *
 * 만약 spring-batch 가 필요한 경우라면 SFN으로 분산처리하게 할것
 */
interface JobTasklet {

    suspend fun execute(job: Job)

    /**
     * 비동기 찹 처리시 사용됨
     * ex) SFN에서 크롤링한 데이터를 가지고 리포트 작성
     *  */
    @Throws(KnownException.ItemSkipException::class)
    suspend fun onProcessComplete(job: Job) {
        //기본적으로 아무것도 하지않음
    }

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

    /** onProcessComplete 가 구현되어서, resume 을 호출 해야하는지? */
    fun onProcessCompleteOverridden(): Boolean = this::class.declaredMemberFunctions.any { it.name == this::onProcessComplete.name }
}