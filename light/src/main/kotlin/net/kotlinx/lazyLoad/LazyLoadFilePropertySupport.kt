package net.kotlinx.lazyLoad

import net.kotlinx.aws.s3.S3Data
import net.kotlinx.core.ProtocolPrefix
import java.io.File


//==================================================== 간단호출 ======================================================

/** 프로파일 설정 등 상세 설정을 다 할때 */
infix fun File.lazyLoad(block: LazyLoadFileProperty.() -> Unit): LazyLoadFileProperty = LazyLoadFileProperty(this).apply(block)

/** 늦은 초기화  & 중위함수 지원 */
infix fun File.lazyLoad(path: String): LazyLoadFileProperty = lazyLoad {
    this.info = path
}

/** 늦은 초기화  & 중위함수 지원 */
infix fun File.lazyLoad(path: S3Data): LazyLoadFileProperty = this.lazyLoad(path.toFullPath())

/** 늦은 초기화 by SSM */
infix fun File.lazyLoadSsm(path: String): LazyLoadFileProperty = this.lazyLoad("${ProtocolPrefix.SSM}$path")