package net.kotlinx.kopring.spring.batch.component

import com.google.common.collect.Lists
import net.kotlinx.kopring.spring.batch.closeIfAble
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream

/**
 * 스프링배치에서 지원하지 않아서(정확히 확인안해봄) 만듬.
 * 여러 줄의 데이터가 하나의 item을 구성할때 사용한다. (하지만 이렇게 만드는것은 매우 비추!)
 * itemReader에 뭐가 오던간에 스래드 안전하긴 하다...
 * ex) Flat 파일이 구성되어 있지만, 다수의 라인을 읽어서 하나의 item을 구성할때 (옥션에서 만든 자체 전송구조)
 * ex)  <<<ftend>>> 이런식으로 넘어오는 파일
 * */
open class MultilineFormatReader<T, R>(
    /** 원본 리더 */
    private val itemReader: ItemReader<T>,
    /**
     * 현재 라인 정보에서, 이것이 마지막인지 읽을 수 있어야 한다.
     * ex) ,MAX(a.AD_ID) OVER(PARTITION BY c.KWD_NAME)  LAST_AD_ID   /  ORDER BY c.KWD_NAME,a.AD_ID
     */
    private val isItemEnd: (T) -> Boolean,
    /** 객체 변환기  */
    private val converter: (List<T>) -> R
) : ItemReader<R?>, ItemStream {

    /** 실제 ITEM수  */
    private var multilineItem = 0

    /** 전체를 동기화 해서 읽는다.  */
    @Synchronized
    override fun read(): R? {
        val lines: MutableList<T> = Lists.newArrayList()
        while (true) {
            val line = itemReader.read() ?: return null
            //lines가 남아있을때 끝나면 경고해야 할수도 있다. 일단 진행
            lines.add(line)
            if (isItemEnd(line)) {
                val item = converter(lines)
                multilineItem++
                return item
            }
        }
    }

    override fun open(executionContext: ExecutionContext) {}

    override fun update(executionContext: ExecutionContext) = executionContext.putInt("multilineItem", multilineItem)

    override fun close() = itemReader.closeIfAble()

}
