package net.kotlinx.module.job.run

import net.kotlinx.module.job.Job

/**
 * 마커 인터페이스
 */
interface JobRoot {

    companion object {
        /** 일반적인 리턴  */
        const val OK = "ok"

        /** 일반적인 실패  */
        const val FAIL = "fail"
    }

    /**
     * 사용자 업로드 파일이 없는 간단 job
     * ex) athena 결과 다운로드
     */
    interface JobTasklet : JobRoot {
        fun doRun(job: Job)
    }
}


///**
// * 트랜잭션 때문에 별도의 인터페이스로 분리했다. 여기에 트랜잭션을 걸자.
// * 한 로우씩 처리하고, 청크별로 트랜잭션 처리가 되는 작업에 사용한다.
// *
// * ex) 대용량 데이터를 입력받아서 읽고 OLTP로 처리할때
// */
//interface JobCsvUpload : JobRoot {
//
//    /** 사용할 바인더 정의  */
//    val binder: FlatDataBinder
//
//    /** 디폴트로 아무것도 안함. 필요할때 구현할것   */
//    fun open(job: Job)
//
//    /**
//     * 커밋 인터벌 별로 나뉘어서 처리됨
//     * 읽기 성공한 애들만 리턴된다.
//     */
//    @Throws(Exception::class)
//    fun doWrite(items: List<JobLineData>)
//
//    /** 디폴트로 아무것도 안함. 필요할때 구현할것   */
//    fun close(job: Job)
//
//    /** 스래드 수 지정  */
//    var threadCnt: Int
//
//    /** 스래드 수 지정  */
//    var commitInterval: Int
//}

///** 기본값응 정의해준다. */
//abstract class AbstractJobCsvUpload : JobCsvUpload {
//
//    override var threadCnt: Int = 1
//    override var commitInterval: Int = 1000
//
//    override fun open(job: Job) {}
//    override fun close(job: Job) {}
//
//}