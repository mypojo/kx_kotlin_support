package net.kotlinx.spring.batch.component

import org.springframework.batch.item.file.MultiResourceItemWriter
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream
import org.springframework.batch.item.file.ResourceSuffixCreator
import org.springframework.core.io.FileSystemResource
import java.io.File

/**
 * MultiResourceItemWriter 가 빌더를 제공하지 않아서 샘플로 만들었음
 */
class MultiResourceItemWriterBuilder(
    val block: MultiResourceItemWriterBuilder.() -> Unit
) {

    /** 편의상 일반 인터페이스로 지정함. 사실 ResourceAwareItemWriterItemStream 가 와야한다.   */
    lateinit var itemWriter: ResourceAwareItemWriterItemStream<*>

    /** 작업 디렉토리 */
    lateinit var workspace: File

    /** 파일 네임. 파일명을 강제로 다시 string으로 변환해서 재조합 하는거 같음  */
    lateinit var name: String

    /**
     * 기본값은 MS 엑셀이 읽기 가능한 최대수 1048576 기준.
     * 커밋 인터벌의 배수로 체크된다.  10으로 지정하더라도 커밋인터벌이 6이라면 12개씩 분할된다.
     */
    var limit: Int = XLS_LIMIT

    /** 기본 네이밍 률  */
    var resourceSuffixCreator: ResourceSuffixCreator = ResourceSuffixCreator { i: Int -> "N${i.toString().padStart(3, '0')}.csv" }

    /**  빌드  */
    fun <T> build(): MultiResourceItemWriter<T> {
        block(this)
        workspace.mkdirs()
        check(workspace.isDirectory)
        return MultiResourceItemWriter<T>().apply {
            setDelegate(itemWriter as ResourceAwareItemWriterItemStream<in T>?)
            setItemCountLimitPerResource(limit)
            setResourceSuffixCreator(resourceSuffixCreator)
            setResource(FileSystemResource(File(workspace, name + "_")))
        }
    }

    companion object {
        /** 엑셀에서 읽을 수 있는 한계  */
        const val XLS_LIMIT = 1000000
    }
}