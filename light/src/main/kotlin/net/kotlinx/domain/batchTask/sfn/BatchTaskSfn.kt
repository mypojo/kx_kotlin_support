package net.kotlinx.domain.batchTask.sfn

import net.kotlinx.core.Kdsl
import net.kotlinx.domain.batchStep.BatchStepOption
import net.kotlinx.domain.batchStep.BatchStepParameter
import net.kotlinx.domain.batchTask.BatchTaskExecutor
import net.kotlinx.domain.batchTask.batchTaskSk
import net.kotlinx.domain.job.Job
import net.kotlinx.reflect.name

/**
 * BatchTask 를 SFN으로 실행하기 위한 옵션정보들
 * */
class BatchTaskSfn {

    @Kdsl
    constructor(job: Job, block: BatchTaskSfn.() -> Unit = {}) {
        this.job = job
        this.parameter = BatchStepParameter {
            option = BatchStepOption {
                jobPk = BatchTaskExecutor::class.name()  //고정
                jobSk = job.batchTaskSk //실제 job의 pk / sk 정보가 입력됨
                sfnId = "${jobPk}-${jobSk}" //위 2개 키의 조합
            }
        }
        apply(block)
    }

    /**
     * 연결 잡 (DDB UI에 보여지는 데이터)
     * pk & sk 이외로 sfnId를 유지하는 용도로 사용
     * 이 잡은 DDB에서 로드해서 주입 & 끝나고 저장
     *  */
    val job: Job

    /** 기본 파라메터로 일단 생성 */
    val parameter: BatchStepParameter

    //==================================================== 입력데이터 ======================================================

    /** 입력데이터 (인메모리) */
    var batchTaskInputInmemery: BatchTaskInputInmemery? = null

    /** 입력데이터 청크 */
    var batchTaskInputCsv: BatchTaskInputCsv? = null


}

