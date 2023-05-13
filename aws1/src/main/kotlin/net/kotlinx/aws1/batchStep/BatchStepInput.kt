package net.kotlinx.aws1.batchStep

import net.kotlinx.core1.threadlocal.ResourceHolder
import java.io.File
import java.util.*

/** 콘솔 링크 */

class BatchStepInput {

    /** 초기화로 새로 채번. 리트라이시 오버라이드. */
    var sfnId: String = UUID.randomUUID().toString()

    /** 이게 있으면 재시도로 간주. 재시도이면 업로드 안함.  */
    var retrySfnId:String? = null

    /** 미구현.. 결과 디렉토리 청소 */
    var reset: Boolean = false

    /** 로컬에서 S3업로드할 파일을 만들 작업공간  */
    var workDir: File = File(ResourceHolder.getWorkspace(), BatchStepInput::class.simpleName)

    /** S3에 업로드할 json 데이터들 */
    lateinit var datas: List<Any>

    /** 버킷명 키 */
    var bucket: String = BatchStepInput.bucket

    /** S3 경로  키 */
    var key: String = BatchStepInput.key

    companion object {
        const val bucket = "bucket"
        const val key = "key"
    }

}